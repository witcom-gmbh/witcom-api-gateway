package de.witcom.api.command.client.configuration;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ClientConfiguration {
	
	//@Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters = HttpMessageConverters::new;

	@Bean
	public Decoder decoder() {
		//return new SpringDecoder(this.messageConverters);
		return new JacksonDecoder();
	}

	@Bean
	public Encoder encoder() {
		//return new SpringEncoder(this.messageConverters);
		return new JacksonEncoder();
	}


}
