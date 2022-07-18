package de.witcom.api.config;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import redis.embedded.RedisServer;

@Configuration
@Profile("redis-embedded")
public class EmbeddedRedisConfig {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${spring.redis.port}")
    private int redisPort;
	
	private RedisServer redisServer;
	
	@PostConstruct
    public void startRedis() throws IOException {
		
		logger.debug("Starting embedded REDIS");
        redisServer = new RedisServer(redisPort);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }

}
