package org.formentor.graphql.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class GraphqlGatewayApplication {

	private static final String GRAPHQL = "/graphql";   // GraphQL requests
	private static final String REGISTRY = "/registry"; // Registers REST service

	public static void main(String[] args) {
		SpringApplication.run(GraphqlGatewayApplication.class, args);
	}

	@RequestMapping(GRAPHQL)
	@RestController
	public class GraphqlController {
		private final GraphQLProvider graphQLProvider;

		public GraphqlController(GraphQLProvider graphQLProvider) {
			this.graphQLProvider = graphQLProvider;
		}

		@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity graphql(@RequestBody GraphQLRequestBody request) {
			if (request.getQuery() == null) {
				request.setQuery("");
			}
			Object result = graphQLProvider.getGraphQL().execute(request.getQuery());
			return (result != null)? ResponseEntity.ok(result): ResponseEntity.noContent().build();
		}
	}

	@RequestMapping(REGISTRY)
	@RestController
	public class RegistryController {
		private final GraphQLProvider graphQLProvider;

		public RegistryController(GraphQLProvider graphQLProvider) {
			this.graphQLProvider = graphQLProvider;
		}

		@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity list() {
			return ResponseEntity.ok(graphQLProvider.services());
		}

		@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity register(@RequestBody ServiceDto serviceDto) {
			graphQLProvider.register(serviceDto.getName(), serviceDto.getUrl());
			return ResponseEntity.noContent().build();
		}

		@DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity unregister(@RequestParam("service") String name) {
			graphQLProvider.unregister(name);
			return ResponseEntity.noContent().build();
		}
	}
}