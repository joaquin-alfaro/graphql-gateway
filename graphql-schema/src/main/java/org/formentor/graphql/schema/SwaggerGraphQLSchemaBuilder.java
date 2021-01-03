package org.formentor.graphql.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class SwaggerGraphQLSchemaBuilder {

    private final GraphQLSchemaBuilder schemaBuilder;

    public SwaggerGraphQLSchemaBuilder() {
        this.schemaBuilder = new GraphQLSchemaBuilder();
    }

    public SwaggerGraphQLSchemaBuilder swagger(String location) {
        return swagger(new SwaggerParser().read(location));
    }

    public SwaggerGraphQLSchemaBuilder swagger(Swagger swagger) {
        // Types
        List<GraphQLObjectType> objectTypes = swagger.getDefinitions().entrySet()
                .stream()
                .map((definition) -> toGraphQLObjectType(definition.getKey(), definition.getValue()))
                .collect(Collectors.toList());

        // Query & Data fetchers
        List<GraphQLFieldDefinition> queryFields = new ArrayList<>();
        Map<FieldCoordinates, DataFetcher<?>> dataFetchers = new HashMap<>();

        String host = "http://" + swagger.getHost();
        String basePath = swagger.getBasePath();
        swagger.getPaths().entrySet().forEach((path)-> {
            final GraphQLFieldDefinition queryField = pathToGraphQLField(path.getKey(), path.getValue());
            queryFields.add(queryField);
            dataFetchers.put(FieldCoordinates.coordinates("Query", queryField.getName()), buildDataFetcher(host, basePath, path.getKey(), path.getValue()));
        });

        schemaBuilder
                .queryFields(queryFields)
                .objectTypes(objectTypes)
                .dataFetchers(dataFetchers);

        return this;
    }

    public GraphQLSchema build() {
        return schemaBuilder.build();
    }

    /**
     * Maps Swagger model with GraphQL object type
     * @param name
     * @param swaggerModel
     * @return
     */
    private GraphQLObjectType toGraphQLObjectType(String name, Model swaggerModel) {
        List<GraphQLFieldDefinition> fields = swaggerModel.getProperties()
                .entrySet()
                .stream()
                .map(this::propertyToGraphQLField)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return newObject()
                .name(name)
                .fields(fields)
                .build();
    }

    /**
     * Maps Swagger path with GraphQLFieldDefinition
     * @param swaggerPath
     * @return
     */
    private GraphQLFieldDefinition pathToGraphQLField(String name, Path swaggerPath) {
        GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                .name(pathToType(name))
                .type(mapOutputType("", swaggerPath.getGet().getResponses().get("200").getSchema()).orElse(null)); // GraphQLString

        if (swaggerPath.getGet().getParameters() != null) {
            builder.arguments(swaggerPath.getGet().getParameters()
                    .stream()
                    .map(this::parameterToGraphQLArgument)
                    .collect(Collectors.toList())
            );
        }

        return builder.build();
    }

    /**
     * Maps Swagger property with GraphQLField
     *
     * @param property
     * @return
     */
    private Optional<GraphQLFieldDefinition> propertyToGraphQLField(Map.Entry<String, Property> property) {
        Optional<GraphQLOutputType> type = mapOutputType(property.getKey(), property.getValue());
        if (!type.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(newFieldDefinition()
                    .name(property.getKey())
                    .type(type.get())
                    .build()
            );
        }
    }

    /**
     * Maps Swagger parameter with GraphQLArgument
     * @param parameter
     * @return
     */
    private GraphQLArgument parameterToGraphQLArgument(Parameter parameter) {
        return newArgument()
                .name(parameter.getName())
                .type(mapInputType(parameter))
                .build();
    }

    /**
     * Builds DataFetcher for a given query field
     * @return
     */
    private DataFetcher buildDataFetcher(String host, String basePath, String path, Path swaggerPath) {
        final OkHttpClient client = new OkHttpClient();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String url = host + buildPath(basePath, path);
        List<String> pathParams = Optional.ofNullable(swaggerPath.getGet().getParameters()).orElse(Collections.emptyList())
                .stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());

        return new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment dataFetchingEnvironment) throws Exception {
                String urlParams = pathParams
                        .stream()
                        .reduce(url, (acc, curr) -> url.replaceAll(String.format("\\{%s}", curr), dataFetchingEnvironment.getArgument(curr).toString()));
                Request request = new Request.Builder().url(urlParams).build();
                Response response = client.newCall(request).execute();
                final String json = response.body().string();

                return objectMapper.readValue(json, new TypeReference<>(){});
            }
        };
    }

    /**
     * Returns the path for a given list of steps
     * ["/", "/books/"] will return "/books/"
     *
     * @param paths
     * @return
     */
    private String buildPath(String ...paths) {
        final String separator = "/";
        return Arrays.stream(paths).reduce(null, (acc, cur) -> {
            if (acc == null) {
                return cur;
            } else if (acc.lastIndexOf(separator) == acc.length()-1 && cur.indexOf(separator) == 0) {
                return acc + cur.substring(1);
            } else {
                return acc + cur;
            }
        });
    }

    /**
     * Maps swagger type GraphQLType
     * @param fieldName
     * @param swaggerProperty
     * @return
     */
    private Optional<GraphQLOutputType> mapOutputType(String fieldName, Property swaggerProperty) {
        GraphQLOutputType type = null;

        final Map<String, GraphQLScalarType> scalarTypes = new HashMap<>() {
            {put("string", GraphQLString);}
            {put("integer", GraphQLInt);}
        };
        if (isID(fieldName)) {
            type = GraphQLID;
        } else if (scalarTypes.containsKey(swaggerProperty.getType())) {
            type = scalarTypes.get(swaggerProperty.getType());
        } else if (isReference(swaggerProperty)) {
            type = GraphQLTypeReference.typeRef(((RefProperty)swaggerProperty).getSimpleRef());
        } else if (isArray(swaggerProperty)) {
            type = GraphQLList.list(mapOutputType(fieldName, ((ArrayProperty) swaggerProperty).getItems()).orElse(null));
        }

        return Optional.ofNullable(type);
    }

    /**
     * Maps swagger parameter to graphql type
     *
     * @param parameter
     * @return
     */
    private GraphQLInputType mapInputType(Parameter parameter) {
        final String fieldName = parameter.getName();
        String swaggerType = null;
        if (parameter instanceof PathParameter) {
            swaggerType = ((PathParameter) parameter).getType();
        } else if (parameter instanceof BodyParameter) {
            swaggerType = ((ModelImpl)((BodyParameter) parameter).getSchema()).getType();
        }
        final Map<String, GraphQLScalarType> scalarTypes = new HashMap<>() {
            {put("string", GraphQLString);}
            {put("integer", GraphQLInt);}
        };
        if (isID(fieldName)) {
            return GraphQLID;
        } else {
            return scalarTypes.get(swaggerType);
        }
    }

    /**
     * Returns true if swagger property is Array of types
     * @param swaggerProperty
     * @return
     */
    private boolean isArray(Property swaggerProperty) {
        return swaggerProperty instanceof ArrayProperty;
    }

    /**
     * Returns true if swagger property is Type reference
     * @param swaggerProperty
     * @return
     */
    private boolean isReference(Property swaggerProperty) {
        return swaggerProperty instanceof RefProperty;
    }

    /**
     * Returns true if the fieldName refers to the ID field
     * @param fieldName
     * @return
     */
    private boolean isID(String fieldName) {
        return (fieldName.equals("id"));
    }

    private String pathToType(String path) {
        return Arrays.stream(path.split("/"))
                .reduce("", (acc, curr) -> (acc.isBlank())?curr: acc + buildPathName(curr));
    }

    private String buildPathName(String name) {
        /**
         * Builds graphql type name from swagger path
         * /books -> books
         * /books/{id} -> booksById
         * /books/library/{id} -> booksWithLibraryById
         */
        final String PARAM_FORMAT = "By%s";
        final String PATH_FORMAT = "With%s";
        return isParam(name)?String.format(PARAM_FORMAT, capitalize(name.replaceAll("[{}]", ""))): String.format(PATH_FORMAT, capitalize(name));
    }

    private boolean isParam(@NonNull String name) {
        return (name.indexOf("{") == 0 && (name.lastIndexOf("}") == (name.length()-1))) ;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
