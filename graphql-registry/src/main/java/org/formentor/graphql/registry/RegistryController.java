package org.formentor.graphql.registry;


import org.formentor.graphql.server.GraphQLProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/registry")
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
