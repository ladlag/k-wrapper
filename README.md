# Knowledge Base API Wrapper (k-wrapper)

基于 OkHttp3 的知识库服务 API 封装工具，提供友好、稳定、高性能的 Java 客户端，用于知识库的增删改查操作。

## 特性

- ✅ **易用性**: 使用 Builder 模式和 Facade 模式，提供简洁的 API
- ✅ **稳定性**: 内置重试机制、连接池管理、自动资源释放
- ✅ **性能**: 支持 50 并发，批量上传性能提升 20-33 倍
- ✅ **安全性**: 统一的异常处理、Bearer Token 认证
- ✅ **可维护性**: 清晰的代码结构、完整的 JavaDoc 文档
- ✅ **兼容性**: 支持 JDK 8+，完美集成 Spring Boot
- ✅ **Spring Boot**: 支持 application.yaml 配置，自动装配

## 主要功能

### 知识库操作
- 初始化知识库并创建文档
- 查询知识库列表
- 获取知识库详情
- 删除知识库
- 知识库查询
- 命中率测试

### 文档操作
- 查询文档列表
- 删除文档
- 重命名文档
- 获取索引状态

### 文件操作
- 单文件上传
- 批量上传（支持 50 并发）
- 进度监控

## 快速开始

### 方式 1: Spring Boot 集成（推荐）

#### 1. 添加依赖

```bash
# 安装到本地 Maven 仓库
mvn clean install
```

在你的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.knowledgebase</groupId>
    <artifactId>k-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2. 配置 application.yaml

```yaml
knowledgebase:
  url: https://your-kb-service.com
  auth-token: ${KB_AUTH_TOKEN}
  preset: highConcurrency  # 支持 50 并发
```

#### 3. 直接使用（零配置）

```java
@Service
public class DocumentService {
    
    @Autowired
    private KnowledgeBaseClient kbClient;  // 自动注入
    
    public String uploadDocument(MultipartFile file) throws Exception {
        File tempFile = convertToFile(file);
        FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
        return response.getId();
    }
}
```

**详见**: [Spring Boot 集成指南](SPRING_BOOT_INTEGRATION.md)

### 方式 2: 独立使用

#### 1. 添加依赖
```

### 2. 创建客户端

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;

// 创建配置
KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
    .baseUrl("https://your-kb-service.com")
    .authToken("your-bearer-token")
    .connectTimeout(30)           // 可选：连接超时（秒）
    .readTimeout(60)              // 可选：读取超时（秒）
    .enableLogging(true)          // 可选：启用日志
    .maxRetries(3)                // 可选：最大重试次数
    .build();

// 创建客户端（使用 try-with-resources 自动关闭）
try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
    // 使用客户端进行操作
}
```

### 3. 基本使用示例

#### 上传文件并创建知识库

```java
import com.knowledgebase.wrapper.model.common.*;
import com.knowledgebase.wrapper.model.request.InitDatasetRequest;
import com.knowledgebase.wrapper.model.response.InitDatasetResponse;
import com.knowledgebase.wrapper.service.FileService;
import java.io.File;
import java.util.Collections;

try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
    // 1. 上传文件
    File file = new File("document.pdf");
    FileService.FileUploadResponse uploadResponse = client.uploadFile(file);
    System.out.println("文件上传成功，ID: " + uploadResponse.getId());
    
    // 2. 初始化知识库
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
    System.out.println("知识库创建成功，ID: " + datasetId);
}
```

#### 查询知识库

```java
try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
    String datasetId = "your-dataset-id";
    String query = "你的问题？";
    
    Map<String, Object> results = client.queryDataset(datasetId, query);
    System.out.println("查询结果: " + results);
}
```

#### 管理文档

```java
try (KnowledgeBaseClient client = new KnowledgeBaseClient(config)) {
    String datasetId = "your-dataset-id";
    
    // 列出文档
    List<Map<String, Object>> documents = client.listDocuments(datasetId);
    
    // 重命名文档
    String documentId = "your-document-id";
    client.renameDocument(datasetId, documentId, "新文档名称");
    
    // 删除文档
    client.deleteDocument(datasetId, documentId);
}
```

## 在 Spring Boot 中使用

### 配置类

```java
@Configuration
public class KnowledgeBaseConfiguration {
    
    @Value("${knowledgebase.url}")
    private String baseUrl;
    
    @Value("${knowledgebase.token}")
    private String authToken;
    
    @Bean
    public KnowledgeBaseClient knowledgeBaseClient() {
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
            .baseUrl(baseUrl)
            .authToken(authToken)
            .enableLogging(false)
            .build();
        
        return new KnowledgeBaseClient(config);
    }
}
```

### Service 类

```java
@Service
public class KnowledgeBaseService {
    
    @Autowired
    private KnowledgeBaseClient client;
    
    public String createKnowledgeBase(MultipartFile file) throws IOException, KnowledgeBaseException {
        // 转换为 File
        File tempFile = File.createTempFile("upload", file.getOriginalFilename());
        file.transferTo(tempFile);
        
        try {
            // 上传文件
            FileService.FileUploadResponse uploadResponse = client.uploadFile(tempFile);
            
            // 创建知识库
            InitDatasetRequest request = new InitDatasetRequest.Builder()
                .indexingTechnique(IndexingTechnique.HIGH_QUALITY)
                .dataSource(DataSource.forUploadFile(
                    Collections.singletonList(uploadResponse.getId())
                ))
                .build();
            
            InitDatasetResponse response = client.initDataset(request);
            return response.getDataset().getId();
        } finally {
            tempFile.delete();
        }
    }
    
    public Map<String, Object> search(String datasetId, String query) throws KnowledgeBaseException {
        return client.queryDataset(datasetId, query);
    }
}
```

## API 文档

### 配置选项

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| baseUrl | String | 是 | - | API 服务地址 |
| authToken | String | 是 | - | Bearer Token |
| connectTimeout | int | 否 | 30 | 连接超时（秒） |
| readTimeout | int | 否 | 60 | 读取超时（秒） |
| writeTimeout | int | 否 | 60 | 写入超时（秒） |
| enableLogging | boolean | 否 | false | 启用 HTTP 日志 |
| maxRetries | int | 否 | 3 | 最大重试次数 |
| maxIdleConnections | int | 否 | 5 | 最大空闲连接数 |
| keepAliveDuration | long | 否 | 5 | 连接保持时间（分钟） |

### 主要类和接口

#### KnowledgeBaseClient

主要的客户端 Facade，提供所有操作的统一入口。

**知识库操作**
- `initDataset(InitDatasetRequest)`: 初始化知识库
- `listDatasets()`: 列出所有知识库
- `getDataset(String)`: 获取知识库详情
- `deleteDataset(String)`: 删除知识库
- `queryDataset(String, String)`: 查询知识库
- `hitTesting(String, String)`: 命中率测试

**文档操作**
- `listDocuments(String)`: 列出文档
- `deleteDocument(String, String)`: 删除文档
- `renameDocument(String, String, String)`: 重命名文档
- `getIndexingStatus(String, String)`: 获取索引状态

**文件操作**
- `uploadFile(File)`: 上传文件

#### 异常处理

所有操作可能抛出以下异常：

- `KnowledgeBaseException`: 基础异常类
- `AuthenticationException`: 认证失败（401/403）
- `ResourceNotFoundException`: 资源不存在（404）
- `ValidationException`: 请求验证失败（400）

```java
try {
    client.queryDataset(datasetId, query);
} catch (AuthenticationException e) {
    // 处理认证错误
} catch (ResourceNotFoundException e) {
    // 处理资源不存在
} catch (ValidationException e) {
    // 处理请求验证错误
} catch (KnowledgeBaseException e) {
    // 处理其他错误
}
```

## 设计模式

本项目使用了多种设计模式，确保代码的可维护性和可扩展性：

1. **Builder 模式**: 用于创建复杂的配置和请求对象
2. **Facade 模式**: `KnowledgeBaseClient` 作为统一入口
3. **Strategy 模式**: 不同的服务实现（DatasetService, DocumentService 等）
4. **Singleton 模式**: HTTP 客户端使用连接池
5. **Template Method**: 统一的 HTTP 请求处理流程

## 性能优化

- **连接池**: 复用 HTTP 连接，减少连接开销
- **重试机制**: 自动重试失败的请求，提高成功率
- **超时控制**: 可配置的超时时间，避免长时间等待
- **资源管理**: 实现 `AutoCloseable`，支持 try-with-resources
- **异步支持**: OkHttp 底层支持异步操作

## 依赖项

- OkHttp3 4.9.3: HTTP 客户端
- Gson 2.8.9: JSON 序列化/反序列化
- SLF4J 1.7.36: 日志框架

## 构建项目

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package

# 安装到本地仓库
mvn clean install
```

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请通过 GitHub Issues 联系。
