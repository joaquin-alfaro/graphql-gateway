package org.formentor.graphql.server;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/graphql")
@RestController
public class GraphQLController {
    private final GraphQLProvider graphQLProvider;

    public GraphQLController(GraphQLProvider graphQLProvider) {
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
