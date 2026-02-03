package com.example.demo;

import com.knowledgebase.wrapper.client.KnowledgeBaseClient;
import com.knowledgebase.wrapper.exception.KnowledgeBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

/**
 * Spring Boot 集成示例应用
 * 
 * 使用方式：
 * 1. 在 application.yaml 中配置 knowledgebase.url 和 knowledgebase.auth-token
 * 2. 运行此应用
 * 3. KnowledgeBaseClient 会自动注入，无需手动配置
 */
@SpringBootApplication
public class SpringBootIntegrationExample {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootIntegrationExample.class, args);
    }
    
    /**
     * 演示自动注入的 KnowledgeBaseClient
     */
    @Bean
    public CommandLineRunner demo(KnowledgeBaseClient kbClient) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("Spring Boot 集成示例");
            System.out.println("========================================\n");
            
            try {
                // 测试：列出所有数据集
                System.out.println("测试 1: 列出所有数据集");
                List<Map<String, Object>> datasets = kbClient.listDatasets();
                System.out.println("找到 " + datasets.size() + " 个数据集");
                
                if (!datasets.isEmpty()) {
                    System.out.println("\n前 3 个数据集:");
                    datasets.stream().limit(3).forEach(dataset -> {
                        System.out.println("  - ID: " + dataset.get("id"));
                        System.out.println("    名称: " + dataset.get("name"));
                    });
                }
                
                System.out.println("\n✅ 测试成功！KnowledgeBaseClient 已自动配置并正常工作。");
                
            } catch (KnowledgeBaseException e) {
                System.err.println("\n❌ 测试失败: " + e.getMessage());
                if (e.getStatusCode() == 401) {
                    System.err.println("提示: 请检查 auth-token 是否正确");
                }
            } catch (Exception e) {
                System.err.println("\n❌ 未知错误: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n========================================");
            System.out.println("提示: 这只是一个演示。在实际应用中，");
            System.out.println("你可以在任何 @Service 或 @Controller 中");
            System.out.println("通过 @Autowired 注入 KnowledgeBaseClient");
            System.out.println("========================================\n");
        };
    }
}
