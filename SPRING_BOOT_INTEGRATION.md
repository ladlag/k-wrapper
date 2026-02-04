# Spring Boot 集成指南

## 简介

k-wrapper 完全支持以 JAR 形式集成到 JDK 8 Spring Boot 项目中，并支持通过 `application.yaml` 进行配置。

## 快速开始

### 1. 添加 Maven 依赖

#### 方式 1: 安装到本地 Maven 仓库

```bash
# 克隆并安装
git clone https://github.com/ladlag/k-wrapper.git
cd k-wrapper
mvn clean install
```

然后在你的 Spring Boot 项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.knowledgebase</groupId>
    <artifactId>k-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 方式 2: 直接使用 JAR 文件

将 `k-wrapper-1.0.0.jar` 复制到你的项目，然后添加依赖：

```xml
<dependency>
    <groupId>com.knowledgebase</groupId>
    <artifactId>k-wrapper</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/k-wrapper-1.0.0.jar</systemPath>
</dependency>
```

### 2. 配置 application.yaml

在你的 Spring Boot 项目的 `src/main/resources/application.yaml` 中添加配置：

#### 基础配置

```yaml
knowledgebase:
  url: https://your-kb-service.com
  auth-token: your-bearer-token
```

#### 完整配置

```yaml
knowledgebase:
  # 必填项
  url: https://your-kb-service.com
  auth-token: ${KB_AUTH_TOKEN:your-default-token}
  
  # 可选项：性能预设（推荐）
  preset: highConcurrency  # development, production, highConcurrency, batchUpload, lowLatency
  
  # 或者使用自定义配置
  connect-timeout: 30       # 连接超时（秒）
  read-timeout: 120         # 读取超时（秒）
  write-timeout: 120        # 写入超时（秒）
  max-idle-connections: 50  # 最大空闲连接数
  keep-alive-duration: 10   # 连接保持时间（分钟）
  max-retries: 3            # 最大重试次数
  retry-backoff-multiplier: 2000  # 重试退避乘数（毫秒）
  enable-logging: false     # 是否启用 HTTP 日志
```

#### 使用环境变量

```yaml
knowledgebase:
  url: ${KB_SERVICE_URL:https://default-url.com}
  auth-token: ${KB_AUTH_TOKEN}
  preset: ${KB_PRESET:production}
```

### 3. 自动配置（零代码）

k-wrapper 提供 Spring Boot 自动配置，无需编写任何配置类！

只要在 `application.yaml` 中配置了 `knowledgebase.url`，客户端就会自动创建并注入到 Spring 容器中。

### 4. 使用客户端

直接在你的 Service 或 Controller 中注入使用：

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    
    @Autowired
    private KnowledgeBaseClient kbClient;  // 自动注入
    
    public String uploadDocument(MultipartFile file) throws Exception {
        // 转换为 File
        File tempFile = convertToFile(file);
        
        try {
            // 上传文件
            FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
            
            // 创建知识库
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
        return kbClient.queryDataset(datasetId, query);
    }
    
    public List<String> batchUpload(List<File> files) throws Exception {
        // 批量上传，支持 50 并发
        return kbClient.uploadFilesBatch(files, 20);
    }
}
```

## 配置说明

### 性能预设 (preset)

使用预设可以快速配置适合不同场景的性能参数：

| 预设值 | 说明 | 连接数 | 适用场景 |
|--------|------|--------|----------|
| `development` | 开发环境 | 5 | 本地开发测试 |
| `production` | 生产环境 | 20 | 常规生产应用 |
| `highConcurrency` | 高并发 | 50 | 批量操作、高流量 |
| `batchUpload` | 批量上传 | 50 | 大文件批量上传 |
| `lowLatency` | 低延迟 | 30 | 查询密集型应用 |

**推荐：** 使用 `preset` 而不是手动配置所有参数。

### 配置优先级

1. **使用 preset**: 如果配置了 `preset`，则使用预设配置
2. **自定义配置**: 如果没有 `preset`，则使用自定义的各项参数
3. **默认值**: 未配置的参数使用默认值

## 完整示例

### application.yaml

```yaml
spring:
  application:
    name: my-kb-app

knowledgebase:
  url: https://kb.example.com
  auth-token: ${KB_AUTH_TOKEN}
  preset: highConcurrency

server:
  port: 8080

logging:
  level:
    com.knowledgebase: INFO
```

### Controller 示例

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {
    
    @Autowired
    private KnowledgeBaseClient kbClient;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = convertToFile(file);
            FileService.FileUploadResponse response = kbClient.uploadFile(tempFile);
            tempFile.delete();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "fileId", response.getId(),
                "fileName", response.getName()
            ));
        } catch (KnowledgeBaseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/datasets")
    public ResponseEntity<?> listDatasets() {
        try {
            List<Map<String, Object>> datasets = kbClient.listDatasets();
            return ResponseEntity.ok(datasets);
        } catch (KnowledgeBaseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody Map<String, String> request) {
        try {
            String datasetId = request.get("datasetId");
            String query = request.get("query");
            
            Map<String, Object> results = kbClient.queryDataset(datasetId, query);
            return ResponseEntity.ok(results);
        } catch (KnowledgeBaseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
```

## 高级配置

### 自定义 Bean（可选）

如果你需要更多控制，可以禁用自动配置并手动创建 Bean：

```java
import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.config.KnowledgeBaseConfig;
import com.knowledgebase.wrapper.config.PerformancePresets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomKnowledgeBaseConfiguration {
    
    @Value("${knowledgebase.url}")
    private String baseUrl;
    
    @Value("${knowledgebase.auth-token}")
    private String authToken;
    
    @Bean
    @ConditionalOnMissingBean
    public KnowledgeBaseClient customKnowledgeBaseClient() {
        // 自定义配置
        KnowledgeBaseConfig config = new KnowledgeBaseConfig.Builder()
            .baseUrl(baseUrl)
            .authToken(authToken)
            .connectTimeout(60)
            .maxIdleConnections(100)
            // 更多自定义配置...
            .build();
        
        return new KnowledgeBaseClient(config);
    }
}
```

### 多环境配置

#### application-dev.yaml (开发环境)

```yaml
knowledgebase:
  url: http://localhost:8080
  auth-token: dev-token
  preset: development
```

#### application-prod.yaml (生产环境)

```yaml
knowledgebase:
  url: https://kb.prod.example.com
  auth-token: ${KB_PROD_TOKEN}
  preset: highConcurrency
```

## 配置属性参考

### 必填属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `knowledgebase.url` | String | Knowledge Base API 服务地址 |
| `knowledgebase.auth-token` | String | 认证 Token（Bearer Token）|

### 可选属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `knowledgebase.preset` | String | - | 性能预设 |
| `knowledgebase.connect-timeout` | Integer | 30 | 连接超时（秒）|
| `knowledgebase.read-timeout` | Integer | 120 | 读取超时（秒）|
| `knowledgebase.write-timeout` | Integer | 120 | 写入超时（秒）|
| `knowledgebase.max-idle-connections` | Integer | 50 | 最大空闲连接数 |
| `knowledgebase.keep-alive-duration` | Long | 10 | 连接保持时间（分钟）|
| `knowledgebase.max-retries` | Integer | 3 | 最大重试次数 |
| `knowledgebase.retry-backoff-multiplier` | Integer | 2000 | 重试退避乘数（毫秒）|
| `knowledgebase.enable-logging` | Boolean | false | 启用 HTTP 日志 |

## 常见问题

### Q: 如何在 IDEA 中获得配置提示？

**A**: 确保安装了 Spring Boot 插件，k-wrapper 提供了完整的配置元数据，IDEA 会自动识别并提供智能提示。

### Q: 如何验证配置是否生效？

**A**: 启动 Spring Boot 应用，查看日志输出：

```
INFO com.knowledgebase.wrapper.spring.KnowledgeBaseAutoConfiguration - Auto-configuring Knowledge Base Client
INFO com.knowledgebase.wrapper.spring.KnowledgeBaseAutoConfiguration - Base URL: https://your-kb-service.com
INFO com.knowledgebase.wrapper.spring.KnowledgeBaseAutoConfiguration - Knowledge Base Client auto-configured successfully
```

### Q: 如何禁用自动配置？

**A**: 在主配置类中排除自动配置：

```java
@SpringBootApplication(exclude = {KnowledgeBaseAutoConfiguration.class})
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### Q: 支持哪些 Spring Boot 版本？

**A**: 支持 Spring Boot 2.x（2.3.x 及以上）和 Spring Boot 3.x。本项目使用 Spring Boot 2.3.12.RELEASE 进行测试。

### Q: 客户端何时被销毁？

**A**: Spring 容器关闭时会自动调用 `KnowledgeBaseClient.close()` 方法，无需手动清理。

## 总结

✅ **支持 JAR 集成**: 可以通过 Maven 依赖或直接使用 JAR 文件集成到 Spring Boot 项目

✅ **支持 application.yaml**: 通过 `knowledgebase.*` 前缀配置所有参数

✅ **自动配置**: 零代码配置，自动创建并注入 Bean

✅ **JDK 8 兼容**: 完全支持 JDK 8 及以上版本

✅ **开箱即用**: 只需配置 URL 和 Token 即可开始使用

✅ **灵活配置**: 支持预设配置和自定义配置

更多信息请参考：
- [README.md](README.md) - 完整文档
- [BEST_PRACTICES.md](BEST_PRACTICES.md) - 最佳实践
- [HIGH_CONCURRENCY_GUIDE.md](HIGH_CONCURRENCY_GUIDE.md) - 高并发使用指南
