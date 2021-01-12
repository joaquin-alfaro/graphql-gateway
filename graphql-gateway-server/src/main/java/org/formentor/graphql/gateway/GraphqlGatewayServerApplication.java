package org.formentor.graphql.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.formentor.graphql"})
public class GraphqlGatewayServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlGatewayServerApplication.class, args);
	}

}