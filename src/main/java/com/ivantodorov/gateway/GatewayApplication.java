package com.ivantodorov.gateway;

import com.ivantodorov.gateway.domain.model.properties.AppProperties;
import com.ivantodorov.gateway.domain.model.properties.FixerApiProperties;
import com.ivantodorov.gateway.domain.model.properties.RabbitMQProperties;
import com.ivantodorov.gateway.domain.model.properties.RabbitRetryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties({
		FixerApiProperties.class,
		RabbitRetryProperties.class,
		RabbitMQProperties.class,
		AppProperties.class
})
@EnableRetry
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
