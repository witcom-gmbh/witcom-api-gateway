package de.witcom.api.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableAsync
@Log4j2
public class AsyncConfig {

    @Bean(name = "gatewayTaskExecutor")
    public Executor gatewayTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("gateway-task-");
        executor.initialize();
        log.debug(String.format("Configured threadPoolTaskExecutor with pool-size", executor.getPoolSize()));
        return executor;
    }
    
}
