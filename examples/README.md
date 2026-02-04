# Spring Boot 集成示例

本目录包含 Spring Boot 集成的示例代码。

## 文件说明

- **SpringBootIntegrationExample.java** - Spring Boot 应用示例，演示自动配置和依赖注入

## 使用方法

### 1. 创建 Spring Boot 项目

使用 Spring Initializr 或手动创建一个 Spring Boot 项目。

### 2. 添加 k-wrapper 依赖

在项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.knowledgebase</groupId>
    <artifactId>k-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 配置 application.yaml

创建 `src/main/resources/application.yaml`：

```yaml
knowledgebase:
  url: https://your-kb-service.com
  auth-token: your-token-here
  preset: highConcurrency
```

### 4. 复制示例代码

将 `SpringBootIntegrationExample.java` 复制到你的项目中，或者参考其中的代码。

### 5. 运行应用

```bash
mvn spring-boot:run
```

## 核心概念

### 自动配置

k-wrapper 提供了 Spring Boot 自动配置，当检测到以下条件时会自动创建 `KnowledgeBaseClient` Bean：

1. `KnowledgeBaseClient` 类在 classpath 中
2. 配置了 `knowledgebase.url` 属性
3. 没有手动定义 `KnowledgeBaseClient` Bean

### 依赖注入

在任何 Spring 管理的 Bean 中，可以直接注入 `KnowledgeBaseClient`：

```java
@Service
public class MyService {
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    public void doSomething() {
        // 使用 kbClient
    }
}
```

### 配置属性

支持以下配置属性：

| 属性 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `knowledgebase.url` | 是 | - | API 服务地址 |
| `knowledgebase.auth-token` | 是 | - | 认证 Token |
| `knowledgebase.preset` | 否 | - | 性能预设 |
| `knowledgebase.connect-timeout` | 否 | 30 | 连接超时（秒）|
| `knowledgebase.read-timeout` | 否 | 120 | 读取超时（秒）|
| `knowledgebase.max-idle-connections` | 否 | 50 | 最大连接数 |

更多配置请参考 [SPRING_BOOT_INTEGRATION.md](../SPRING_BOOT_INTEGRATION.md)

## 更多示例

### Service 层使用

```java
@Service
public class DocumentService {
    
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    public String uploadDocument(MultipartFile file) throws Exception {
        File tempFile = convertToFile(file);
        try {
            FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
            return response.getId();
        } finally {
            tempFile.delete();
        }
    }
    
    public Map<String, Object> searchDataset(String datasetId, String query) 
            throws KnowledgeBaseException {
        return kbClient.queryDataset(datasetId, query);
    }
    
    public List<String> batchUpload(List<File> files) throws KnowledgeBaseException {
        return kbClient.uploadFilesBatch(files, 20);
    }
}
```

### Controller 层使用

```java
@RestController
@RequestMapping("/api/kb")
public class KnowledgeBaseController {
    
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    @GetMapping("/datasets")
    public ResponseEntity<?> listDatasets() {
        try {
            List<Map<String, Object>> datasets = kbClient.listDatasets();
            return ResponseEntity.ok(datasets);
        } catch (KnowledgeBaseException e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = convertToFile(file);
            FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
            tempFile.delete();
            
            return ResponseEntity.ok(Map.of(
                "fileId", response.getId(),
                "fileName", response.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
}
```

## 故障排查

### 客户端未自动配置

检查以下几点：

1. **检查依赖**: 确保 k-wrapper 已添加到 pom.xml
2. **检查配置**: 确保 application.yaml 中配置了 `knowledgebase.url`
3. **检查日志**: 启动时应该看到自动配置的日志：
   ```
   INFO com.knowledgebase.wrapper.spring.KnowledgeBaseAutoConfiguration - Auto-configuring Knowledge Base Client
   ```

### 401 认证错误

检查 `knowledgebase.auth-token` 是否正确配置。

### 连接超时

增加超时时间或使用更合适的预设：

```yaml
knowledgebase:
  preset: batchUpload  # 使用更长的超时时间
```

## 参考文档

- [Spring Boot 集成指南](../SPRING_BOOT_INTEGRATION.md)
- [最佳实践](../BEST_PRACTICES.md)
- [高并发使用指南](../HIGH_CONCURRENCY_GUIDE.md)
