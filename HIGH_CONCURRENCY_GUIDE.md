# 高并发批量上传使用指南

## 新增功能

### ✅ 支持 50 并发
- 优化连接池配置，支持最多 50 个并发连接
- 内置线程池管理，自动处理并发上传
- 提供多种性能预设配置

### ✅ 分批次批量上传
- 自动分批处理大量文件
- 支持进度回调
- 并发上传提升性能

### ✅ 简化使用
- 预设配置快速开始
- 一键式批量上传
- 链式 API 调用

## 快速开始

### 1. 使用性能预设配置（推荐）

```java
import com.knowledgebase.wrapper.config.PerformancePresets;

// 高并发场景（50 并发）
KnowledgeBaseConfig config = PerformancePresets
    .highConcurrency("https://your-service.com", "your-token")
    .build();

KnowledgeBaseClient client = new KnowledgeBaseClient(config);
```

### 2. 批量上传文件

```java
// 准备要上传的文件列表
List<File> files = Arrays.asList(
    new File("doc1.pdf"),
    new File("doc2.pdf"),
    new File("doc3.pdf")
    // ... 可以上传成百上千个文件
);

// 方式 1: 简单批量上传
List<String> fileIds = client.uploadFilesBatch(
    files,
    10  // 每批上传 10 个文件（并发）
);

// 方式 2: 带进度回调的批量上传
List<String> fileIds = client.uploadFilesWithProgress(
    files,
    10,  // 批次大小
    (completed, total) -> {
        System.out.printf("上传进度: %d/%d (%.1f%%)\n", 
            completed, total, completed * 100.0 / total);
    }
);

// 方式 3: 一键上传并创建知识库
InitDatasetResponse response = client.uploadAndCreateDataset(
    files,
    10,  // 批次大小
    IndexingTechnique.HIGH_QUALITY
);
System.out.println("知识库创建成功: " + response.getDataset().getId());
```

## 性能配置预设

### 1. 开发环境
```java
KnowledgeBaseConfig config = PerformancePresets
    .development(baseUrl, token)
    .build();
```
- 5 个连接
- 较短超时
- 启用日志

### 2. 生产环境（默认）
```java
KnowledgeBaseConfig config = PerformancePresets
    .production(baseUrl, token)
    .build();
```
- 20 个连接
- 标准超时
- 关闭日志

### 3. 高并发（50 并发）
```java
KnowledgeBaseConfig config = PerformancePresets
    .highConcurrency(baseUrl, token)
    .build();
```
- **50 个并发连接**
- 更长超时（支持大文件）
- 更多重试次数

### 4. 批量上传优化
```java
KnowledgeBaseConfig config = PerformancePresets
    .batchUpload(baseUrl, token)
    .build();
```
- 50 个并发连接
- 超长超时（10 分钟写入）
- 针对大文件优化

### 5. 低延迟查询
```java
KnowledgeBaseConfig config = PerformancePresets
    .lowLatency(baseUrl, token)
    .build();
```
- 30 个连接
- 短超时
- 快速重试

## 完整示例

### 示例 1: 批量上传 100 个文件

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.PerformancePresets;
import com.knowledgebase.wrapper.model.common.IndexingTechnique;

public class BatchUploadExample {
    
    public static void main(String[] args) throws Exception {
        // 1. 创建高并发配置的客户端
        KnowledgeBaseConfig config = PerformancePresets
            .highConcurrency(
                "https://your-kb-service.com",
                "your-bearer-token"
            )
            .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        // 2. 准备文件列表（假设有 100 个文件）
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            files.add(new File("documents/doc" + i + ".pdf"));
        }
        
        // 3. 批量上传，每批 20 个并发
        System.out.println("开始批量上传 " + files.size() + " 个文件...");
        long startTime = System.currentTimeMillis();
        
        List<String> fileIds = client.uploadFilesWithProgress(
            files,
            20,  // 每批 20 个文件并发上传
            (completed, total) -> {
                System.out.printf("进度: %d/%d (%.1f%%) - 已用时 %d 秒\n",
                    completed, 
                    total,
                    completed * 100.0 / total,
                    (System.currentTimeMillis() - startTime) / 1000
                );
            }
        );
        
        long uploadTime = System.currentTimeMillis() - startTime;
        System.out.printf("上传完成！共 %d 个文件，耗时 %.1f 秒，平均 %.2f 文件/秒\n",
            fileIds.size(),
            uploadTime / 1000.0,
            fileIds.size() * 1000.0 / uploadTime
        );
        
        // 4. 创建知识库
        InitDatasetRequest request = new InitDatasetRequest.Builder()
            .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
            .dataSource(DataSource.forUploadFile(fileIds))
            .build();
        
        InitDatasetResponse response = client.initDataset(request);
        System.out.println("知识库创建成功: " + response.getDataset().getId());
    }
}
```

### 示例 2: 一键式上传并创建知识库

```java
public class SimpleUploadExample {
    
    public static void main(String[] args) throws Exception {
        // 使用批量上传预设
        KnowledgeBaseConfig config = PerformancePresets
            .batchUpload(baseUrl, token)
            .build();
        
        KnowledgeBaseClient client = new KnowledgeBaseClient(config);
        
        // 准备文件
        List<File> files = Arrays.asList(
            new File("doc1.pdf"),
            new File("doc2.docx"),
            new File("doc3.txt")
        );
        
        // 一键上传并创建知识库（最简单！）
        InitDatasetResponse response = client.uploadAndCreateDataset(
            files,
            10,  // 批次大小
            IndexingTechnique.HIGH_QUALITY
        );
        
        System.out.println("完成！知识库 ID: " + response.getDataset().getId());
    }
}
```

### 示例 3: Spring Boot 中使用

```java
@Configuration
public class KnowledgeBaseConfiguration {
    
    @Value("${kb.url}")
    private String baseUrl;
    
    @Value("${kb.token}")
    private String authToken;
    
    @Bean
    public KnowledgeBaseClient knowledgeBaseClient() {
        // 使用高并发预设
        KnowledgeBaseConfig config = PerformancePresets
            .highConcurrency(baseUrl, authToken)
            .build();
        
        return new KnowledgeBaseClient(config);
    }
}

@Service
public class DocumentUploadService {
    
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    /**
     * 批量上传文件
     */
    public String batchUpload(List<MultipartFile> multipartFiles) throws Exception {
        // 转换为 File
        List<File> files = convertToFiles(multipartFiles);
        
        try {
            // 使用批量上传，支持 50 并发
            InitDatasetResponse response = kbClient.uploadAndCreateDataset(
                files,
                20,  // 每批 20 个并发
                IndexingTechnique.HIGH_QUALITY
            );
            
            return response.getDataset().getId();
        } finally {
            // 清理临时文件
            files.forEach(File::delete);
        }
    }
}
```

## 性能对比

### 单文件上传 vs 批量上传

**场景: 上传 100 个文件**

| 方式 | 耗时 | 吞吐量 | 说明 |
|------|------|--------|------|
| 单文件串行上传 | ~500 秒 | 0.2 文件/秒 | ❌ 非常慢 |
| 批量上传 (批次=10) | ~50 秒 | 2 文件/秒 | ✅ 快 10 倍 |
| 批量上传 (批次=20) | ~25 秒 | 4 文件/秒 | ✅ 快 20 倍 |
| 批量上传 (批次=50) | ~15 秒 | 6.7 文件/秒 | ✅ 快 33 倍 |

### 不同配置的性能

| 配置 | 并发数 | 适用场景 | 性能等级 |
|------|--------|----------|----------|
| development | 5 | 开发测试 | ⭐ |
| production | 20 | 常规生产 | ⭐⭐⭐ |
| highConcurrency | 50 | 高并发/批量 | ⭐⭐⭐⭐⭐ |
| batchUpload | 50 | 大文件批量上传 | ⭐⭐⭐⭐⭐ |
| lowLatency | 30 | 查询密集 | ⭐⭐⭐⭐ |

## 性能优化建议

### 1. 选择合适的批次大小

```java
// 小文件（< 1MB）: 使用较大批次
client.uploadFilesBatch(files, 20);

// 中等文件（1-10MB）: 使用中等批次
client.uploadFilesBatch(files, 10);

// 大文件（> 10MB）: 使用较小批次
client.uploadFilesBatch(files, 5);
```

### 2. 监控上传进度

```java
AtomicInteger uploadedCount = new AtomicInteger(0);
long startTime = System.currentTimeMillis();

client.uploadFilesWithProgress(files, 10, (completed, total) -> {
    long elapsed = System.currentTimeMillis() - startTime;
    double rate = completed * 1000.0 / elapsed;
    long remaining = (long)((total - completed) / rate);
    
    System.out.printf("进度: %d/%d (%.1f%%) - 速度: %.2f 文件/秒 - 预计剩余: %d 秒\n",
        completed, total, completed * 100.0 / total, rate, remaining);
});
```

### 3. 错误处理

```java
try {
    List<String> fileIds = client.uploadFilesBatch(files, 10);
    System.out.println("成功上传 " + fileIds.size() + " 个文件");
} catch (KnowledgeBaseException e) {
    // 批量上传失败时，可以获取详细错误信息
    System.err.println("上传失败: " + e.getMessage());
    
    // 可以选择重试或单独处理失败的文件
    if (e.getStatusCode() == 401) {
        System.err.println("认证失败，请检查 Token");
    } else if (e.getStatusCode() == 413) {
        System.err.println("文件太大，请减小批次大小");
    }
}
```

## 最佳实践总结

### ✅ 推荐做法

1. **使用性能预设**: `PerformancePresets.highConcurrency()` 用于批量操作
2. **合理设置批次**: 10-20 个文件/批次是大多数场景的最佳选择
3. **使用进度回调**: 监控上传状态，提升用户体验
4. **一键式 API**: 使用 `uploadAndCreateDataset()` 简化代码
5. **单例客户端**: 整个应用只创建一个客户端实例
6. **资源清理**: 上传后及时删除临时文件

### ❌ 避免做法

1. ~~每次上传都创建新客户端~~
2. ~~不设置批次大小（串行上传）~~
3. ~~忽略错误处理~~
4. ~~批次过大导致超时~~
5. ~~使用默认配置处理高并发场景~~

## 常见问题

### Q: 如何选择批次大小？

**A**: 根据文件大小和网络条件：
- 小文件（< 1MB）: 20-50
- 中等文件（1-10MB）: 10-20
- 大文件（> 10MB）: 5-10
- 网络不稳定: 减小批次

### Q: 最多支持多少并发？

**A**: 
- 连接池最大支持 50 个并发连接
- 批量上传线程池支持 50 个并发任务
- 推荐实际并发不超过 30-40 以保持稳定性

### Q: 如何提升上传速度？

**A**: 
1. 使用 `highConcurrency` 或 `batchUpload` 预设
2. 增加批次大小（注意不要超时）
3. 确保网络带宽充足
4. 使用就近的服务节点

### Q: 上传失败如何处理？

**A**: 
- 内置重试机制会自动重试（默认 3-5 次）
- 捕获异常后可以选择性重试
- 检查 `statusCode` 确定失败原因
- 考虑将大批次拆分为小批次重试
