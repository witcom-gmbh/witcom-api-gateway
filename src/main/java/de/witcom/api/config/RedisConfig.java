package de.witcom.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RedisConfig {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
	    logger.debug("Configuring REDIS");
		return new StringRedisTemplate(connectionFactory);
	}
    
}

