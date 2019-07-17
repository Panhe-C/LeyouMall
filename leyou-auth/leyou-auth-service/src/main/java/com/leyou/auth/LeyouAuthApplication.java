package com.leyou.auth;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})//启动时候报错：Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.（因为引入了user-service的依赖）
@EnableDiscoveryClient
@EnableFeignClients
public class LeyouAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeyouAuthApplication.class);
    }
}
