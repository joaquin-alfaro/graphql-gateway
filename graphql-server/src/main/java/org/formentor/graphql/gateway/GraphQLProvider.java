package org.formentor.graphql.gateway;

import graphql.GraphQL;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.formentor.graphql.schema.SwaggerGraphQLSchemaBuilder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class GraphQLProvider {

    private Map<String, Swagger> services = new HashMap<>();
    private GraphQL graphQL;

    /**
     * Registers a REST service
     * @param name
     * @param location
     */
    public void register(String name, String location) {
        services.put(name, new SwaggerParser().read(location));
        load();
    }

    /**
     * Registers a REST service
     * @param name
     */
    public void unregister(String name) {
        services.remove(name);
        load();
    }

    public Collection<String> services() {
        return services.keySet();
    }

    /**
     * Loads REST services in GraphQL schema
     */
    private void load() {
        SwaggerGraphQLSchemaBuilder graphQLConverter = new SwaggerGraphQLSchemaBuilder();
        services.values().stream().forEach(swagger -> graphQLConverter.swagger(swagger));
        this.graphQL = GraphQL.newGraphQL(graphQLConverter.build()).build();
    }

    public GraphQL getGraphQL() {
        /**
         * TODO
         * Controls error when graphQL is null as there is no service registered
         */
        return graphQL;
    }
}
