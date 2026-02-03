# Knowledge Base API Wrapper - Quick Start Guide

## 5分钟快速开始

### 步骤 1: 获取 JAR 包

```bash
# 克隆仓库
git clone https://github.com/ladlag/k-wrapper.git
cd k-wrapper

# 构建项目
mvn clean package

# JAR 文件位于 target/k-wrapper-1.0.0.jar
```

### 步骤 2: 添加依赖到你的项目

#### Maven 项目

将 JAR 安装到本地 Maven 仓库：

```bash
mvn clean install
```

然后在你的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.knowledgebase</groupId>
    <artifactId>k-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 直接使用 JAR

将 `k-wrapper-1.0.0.jar` 复制到你的项目，并确保以下依赖也在 classpath 中：
- okhttp-4.9.3.jar
- okhttp-logging-interceptor-4.9.3.jar
- gson-2.8.9.jar
- slf4j-api-1.7.36.jar

### 步骤 3: 编写第一个程序

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.model.common.*;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import com.knowledgebase.wrapper.service.FileService;

import java.io.File;
import java.util.Collections;

public class QuickStartExample {
    public static void main(String[] args) throws Exception {
        // 1. 创建配置
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
            .baseUrl("https://your-kb-service.com")
            .authToken("your-bearer-token")
            .enableLogging(true)
            .build();
        
        // 2. 创建客户端
        try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
            
            // 3. 上传文件
            File file = new File("document.pdf");
            FileService.FileUploadResponse uploadResponse = client.uploadFile(file);
            System.out.println("文件已上传: " + uploadResponse.getId());
            
            // 4. 创建知识库
            InitDatasetRequest request = new InitDatasetRequest.Builder()
                .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                .dataSource(DataSource.forUploadFile(
                    Collections.singletonList(uploadResponse.getId())
                ))
                .embeddingModel("text-embedding-v2")
                .embeddingModelProvider("your-provider")
                .build();
            
            InitDatasetResponse response = client.initDataset(request);
            String datasetId = response.getDataset().getId();
            System.out.println("知识库已创建: " + datasetId);
            
            // 5. 查询知识库
            Map<String, Object> results = client.queryDataset(
                datasetId, 
                "文档的主要内容是什么？"
            );
            System.out.println("查询结果: " + results);
        }
    }
}
```

## 常见使用场景

### 场景 1: 批量上传文件创建知识库

```java
// 上传多个文件
List<String> fileIds = new ArrayList<>();
for (File file : files) {
    FileService.FileUploadResponse response = client.uploadFile(file);
    fileIds.add(response.getId());
}

// 创建包含所有文件的知识库
InitDatasetRequest request = new InitDatasetRequest.Builder()
    .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
    .dataSource(DataSource.forUploadFile(fileIds))
    .build();

InitDatasetResponse response = client.initDataset(request);
```

### 场景 2: 查询并管理文档

```java
// 列出所有文档
List<Map<String, Object>> documents = client.listDocuments(datasetId);

// 重命名文档
String documentId = (String) documents.get(0).get("id");
client.renameDocument(datasetId, documentId, "新文档名");

// 删除文档
client.deleteDocument(datasetId, documentId);
```

### 场景 3: 监控索引状态

```java
// 获取批次索引状态
String batchId = response.getBatch();
Map<String, Object> status = client.getIndexingStatus(datasetId, batchId);

// 等待索引完成
while (!"completed".equals(status.get("indexing_status"))) {
    Thread.sleep(5000);
    status = client.getIndexingStatus(datasetId, batchId);
}
```

### 场景 4: Spring Boot 集成

```java
@Service
public class DocumentService {
    
    private final KnowledgeBaseClient kbClient;
    
    @Autowired
    public DocumentService(KnowledgeBaseClient kbClient) {
        this.kbClient = kbClient;
    }
    
    @Transactional
    public String uploadAndCreateKnowledgeBase(MultipartFile file) throws Exception {
        // 转换并上传文件
        File tempFile = convertMultipartFileToFile(file);
        try {
            FileService.FileUploadResponse uploadResponse = 
                kbClient.uploadFile(tempFile);
            
            // 创建知识库
            InitDatasetRequest request = new InitDatasetRequest.Builder()
                .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                .dataSource(DataSource.forUploadFile(
                    Collections.singletonList(uploadResponse.getId())
                ))
                .build();
            
            InitDatasetResponse response = kbClient.initDataset(request);
            return response.getDataset().getId();
        } finally {
            tempFile.delete();
        }
    }
    
    public List<Map<String, Object>> searchKnowledgeBase(
            String datasetId, 
            String query) throws Exception {
        Map<String, Object> results = kbClient.queryDataset(datasetId, query);
        return (List<Map<String, Object>>) results.get("data");
    }
}
```

## 配置最佳实践

### 生产环境配置

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .baseUrl(System.getenv("KB_SERVICE_URL"))
    .authToken(System.getenv("KB_AUTH_TOKEN"))
    .connectTimeout(30)
    .readTimeout(120)  // 增加超时时间
    .maxRetries(5)     // 增加重试次数
    .maxIdleConnections(10)  // 增加连接池大小
    .keepAliveDuration(10)   // 延长保活时间
    .enableLogging(false)    // 生产环境关闭日志
    .build();
```

### 开发环境配置

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .baseUrl("http://localhost:8080")
    .authToken("dev-token")
    .enableLogging(true)  // 开发环境开启日志
    .build();
```

## 异常处理

```java
try {
    client.queryDataset(datasetId, query);
} catch (AuthenticationException e) {
    // Token 过期或无效
    log.error("认证失败: {}", e.getMessage());
    // 刷新 token 并重试
} catch (ResourceNotFoundException e) {
    // 知识库或文档不存在
    log.error("资源不存在: {}", e.getMessage());
} catch (ValidationException e) {
    // 请求参数错误
    log.error("参数验证失败: {}", e.getMessage());
} catch (KnowledgeBaseException e) {
    // 其他错误
    log.error("操作失败: {} (状态码: {})", 
        e.getMessage(), e.getStatusCode());
}
```

## 性能优化建议

1. **复用客户端实例**: 创建一个全局的 `KnowledgeBaseClient` 实例，避免频繁创建
2. **合理设置连接池**: 根据并发量调整 `maxIdleConnections`
3. **批量操作**: 使用批量上传功能，减少网络请求
4. **异步处理**: 对于耗时操作，可以考虑异步执行
5. **缓存查询结果**: 对于相同的查询，考虑使用缓存

## 常见问题

### Q: 如何处理大文件上传？

A: 增加写入超时时间：

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .writeTimeout(300)  // 5分钟
    .build();
```

### Q: 如何处理并发请求？

A: OkHttp 内置了连接池，自动处理并发：

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .maxIdleConnections(20)  // 增加连接池大小
    .build();
```

### Q: 如何自定义重试策略？

A: 通过配置 `maxRetries` 参数，系统会自动使用指数退避策略：

```java
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .maxRetries(5)  // 最多重试5次
    .build();
```

## 更多示例

查看 `src/main/java/com/knowledgebase/wrapper/UsageExample.java` 获取更多完整示例。

## 技术支持

- GitHub Issues: https://github.com/ladlag/k-wrapper/issues
- 文档: 查看 README.md
