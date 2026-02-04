# 最佳实践指南 / Best Practices Guide

## 客户端生命周期管理

### ❌ 错误做法：每次操作都创建新客户端

```java
// 不推荐！每次都创建新客户端会导致性能问题
public void badExample() {
    try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
        client.queryDataset(datasetId, query);
    }
}
```

**问题**：
- 频繁创建/销毁连接池
- 无法复用 HTTP 连接
- 浪费系统资源

### ✅ 正确做法：应用级别单例

#### 方案 1: 手动管理单例（非 Spring）

```java
public class KnowledgeBaseManager {
    
    private static volatile KnowledgeBaseClient instance;
    private static final Object lock = new Object();
    
    /**
     * 获取全局单例客户端
     */
    public static KnowledgeBaseClient getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
                        .baseUrl(System.getenv("KB_SERVICE_URL"))
                        .authToken(System.getenv("KB_AUTH_TOKEN"))
                        .enableLogging(false)
                        .build();
                    
                    instance = new KnowledgeBaseClient(config);
                    
                    // 注册关闭钩子，应用退出时关闭客户端
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (instance != null) {
                            instance.close();
                        }
                    }));
                }
            }
        }
        return instance;
    }
    
    /**
     * 使用示例
     */
    public static void usage() throws Exception {
        KnowledgeBaseClient client = KnowledgeBaseManager.getInstance();
        
        // 多次使用同一个客户端实例
        client.listDatasets();
        client.queryDataset(datasetId, query1);
        client.queryDataset(datasetId, query2);
        // ... 更多操作
        
        // 不需要手动关闭，应用退出时自动关闭
    }
}
```

#### 方案 2: Spring Boot 集成（推荐）

```java
@Configuration
public class KnowledgeBaseConfiguration {
    
    @Value("${knowledgebase.url}")
    private String baseUrl;
    
    @Value("${knowledgebase.token}")
    private String authToken;
    
    /**
     * 创建客户端 Bean - 应用级别单例
     */
    @Bean
    @ConditionalOnMissingBean
    public KnowledgeBaseClient knowledgeBaseClient() {
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
            .baseUrl(baseUrl)
            .authToken(authToken)
            .connectTimeout(30)
            .readTimeout(120)
            .maxIdleConnections(10)
            .keepAliveDuration(10)
            .enableLogging(false)
            .build();
        
        return new KnowledgeBaseClient(config);
    }
    
    /**
     * 应用关闭时销毁客户端
     */
    @PreDestroy
    public void cleanup() {
        // Spring 会自动调用 close() 方法，因为 KnowledgeBaseClient 实现了 AutoCloseable
    }
}

/**
 * Service 中使用
 */
@Service
public class DocumentService {
    
    // 注入单例客户端
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    public String uploadDocument(MultipartFile file) throws Exception {
        // 直接使用，不需要创建新实例
        File tempFile = convertToFile(file);
        try {
            FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
            
            InitDatasetRequest request = new InitDatasetRequest.Builder()
                .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                .dataSource(DataSource.forUploadFile(
                    Collections.singletonList(response.getId())
                ))
                .build();
            
            InitDatasetResponse dataset = kbClient.initDataset(request);
            return dataset.getDataset().getId();
        } finally {
            tempFile.delete();
        }
    }
    
    public Map<String, Object> search(String datasetId, String query) throws Exception {
        // 复用同一个客户端实例
        return kbClient.queryDataset(datasetId, query);
    }
    
    // 不需要手动关闭客户端，Spring 容器会管理
}
```

#### 方案 3: 资源池模式（高并发场景）

```java
public class KnowledgeBaseClientPool {
    
    private final List<KnowledgeBaseClient> pool;
    private final Semaphore semaphore;
    private final KnowledgeBaseConfig config;
    
    public KnowledgeBaseClientPool(KnowledgeBaseConfig config, int poolSize) {
        this.config = config;
        this.pool = new ArrayList<>(poolSize);
        this.semaphore = new Semaphore(poolSize);
        
        for (int i = 0; i < poolSize; i++) {
            pool.add(new KnowledgeBaseClient(config));
        }
    }
    
    /**
     * 借用客户端
     */
    public KnowledgeBaseClient borrowClient() throws InterruptedException {
        semaphore.acquire();
        synchronized (pool) {
            return pool.remove(0);
        }
    }
    
    /**
     * 归还客户端
     */
    public void returnClient(KnowledgeBaseClient client) {
        synchronized (pool) {
            pool.add(client);
        }
        semaphore.release();
    }
    
    /**
     * 关闭所有客户端
     */
    public void shutdown() {
        for (KnowledgeBaseClient client : pool) {
            client.close();
        }
    }
    
    /**
     * 使用示例
     */
    public void usage() throws Exception {
        KnowledgeBaseClient client = borrowClient();
        try {
            client.queryDataset(datasetId, query);
        } finally {
            returnClient(client);
        }
    }
}
```

## 配置优化

### 生产环境推荐配置

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .baseUrl(serviceUrl)
    .authToken(authToken)
    
    // 超时配置
    .connectTimeout(30)      // 连接超时 30 秒
    .readTimeout(120)        // 读取超时 2 分钟（考虑大文件上传）
    .writeTimeout(120)       // 写入超时 2 分钟
    
    // 连接池配置（关键！）
    .maxIdleConnections(20)  // 最大空闲连接数（根据并发量调整）
    .keepAliveDuration(10)   // 连接保持 10 分钟
    
    // 重试配置
    .maxRetries(5)           // 最多重试 5 次
    .retryBackoffMultiplier(2000)  // 重试间隔 2s, 4s, 6s...
    
    // 日志配置
    .enableLogging(false)    // 生产环境关闭详细日志
    .build();
```

## 线程安全说明

`KnowledgeBaseClient` 是**完全线程安全**的，可以在多线程环境中共享使用：

```java
@Service
public class ConcurrentService {
    
    @Autowired
    private KnowledgeBaseClient kbClient;  // 单例，线程安全
    
    @Async
    public CompletableFuture<Map<String, Object>> searchAsync(String datasetId, String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 多个线程同时调用是安全的
                return kbClient.queryDataset(datasetId, query);
            } catch (KnowledgeBaseException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

## Token 刷新策略

如果 Token 会过期，建议实现 Token 刷新机制：

```java
@Component
public class KnowledgeBaseClientProvider {
    
    @Value("${knowledgebase.url}")
    private String baseUrl;
    
    private volatile KnowledgeBaseClient client;
    private final Object lock = new Object();
    
    /**
     * 获取客户端，自动处理 Token 过期
     */
    public KnowledgeBaseClient getClient() {
        if (client == null) {
            synchronized (lock) {
                if (client == null) {
                    client = createNewClient();
                }
            }
        }
        return client;
    }
    
    /**
     * Token 过期时刷新客户端
     */
    public void refreshClient() {
        synchronized (lock) {
            if (client != null) {
                client.close();
            }
            client = createNewClient();
        }
    }
    
    private KnowledgeBaseClient createNewClient() {
        String token = fetchNewToken();  // 从认证服务获取新 Token
        
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
            .baseUrl(baseUrl)
            .authToken(token)
            .build();
        
        return new KnowledgeBaseClient(config);
    }
    
    private String fetchNewToken() {
        // 实现 Token 获取逻辑
        return "new-token";
    }
    
    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }
}

@Service
public class SmartService {
    
    @Autowired
    private KnowledgeBaseClientProvider clientProvider;
    
    public Map<String, Object> searchWithAutoRefresh(String datasetId, String query) {
        try {
            return clientProvider.getClient().queryDataset(datasetId, query);
        } catch (AuthenticationException e) {
            // Token 过期，刷新并重试
            clientProvider.refreshClient();
            try {
                return clientProvider.getClient().queryDataset(datasetId, query);
            } catch (KnowledgeBaseException ex) {
                throw new RuntimeException("Failed after token refresh", ex);
            }
        } catch (KnowledgeBaseException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 性能监控

### 添加性能监控

```java
@Aspect
@Component
public class KnowledgeBaseMonitor {
    
    @Around("execution(* com.knowledgebase.wrapper.client.KnowledgeBaseClient.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录性能指标
            log.info("KB API Call: {} completed in {}ms", methodName, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("KB API Call: {} failed after {}ms", methodName, duration, e);
            throw e;
        }
    }
}
```

## 总结

### ✅ 应该做的
1. **创建应用级别的单例客户端**
2. **在 Spring 中作为 Bean 管理**
3. **复用连接和连接池**
4. **只在应用关闭时关闭客户端**
5. **合理配置连接池大小**

### ❌ 不应该做的
1. ~~每次操作都创建新客户端~~
2. ~~在方法内部使用 try-with-resources~~
3. ~~频繁创建和销毁客户端~~
4. ~~不配置连接池参数~~

### 性能对比

```
错误方式（每次创建）:
- 1000 次请求 ≈ 30-50 秒
- 内存占用高
- CPU 开销大

正确方式（单例复用）:
- 1000 次请求 ≈ 5-10 秒
- 内存占用低
- CPU 开销小
- 性能提升 3-5 倍
```
