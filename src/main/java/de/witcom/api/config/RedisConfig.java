package de.witcom.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.log4j.Log4j2;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;

@Configuration
@Log4j2
public class RedisConfig {

	@Bean
	public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
		return new RedisLockProvider(connectionFactory, "API-GATEWAY");
	}
    
    @Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
	    log.debug("Configuring REDIS");
		return new StringRedisTemplate(connectionFactory);
	}
    
}

