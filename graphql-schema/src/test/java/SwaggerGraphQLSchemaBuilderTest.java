import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.formentor.graphql.schema.SwaggerGraphQLSchemaBuilder;
import org.junit.jupiter.api.Test;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.FieldCoordinates.coordinates;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SwaggerGraphQLSchemaBuilderTest {
    private static final String SWAGGER_LOCATION = "src/test/resources/books-swagger.json"; // "http://localhost:8081/v2/api-docs"
    public static final String SWAGGER_DEFINITION_AUTHOR = "AuthorDto";
    public static final String SWAGGER_DEFINITION_BOOK = "BookDto";

    @Test
    public void build_has_to_declare_Query() {
    // Given
        final SwaggerGraphQLSchemaBuilder swaggerGraphQLSchemaBuilder = new SwaggerGraphQLSchemaBuilder();
        final Swagger swagger = new SwaggerParser().read(SWAGGER_LOCATION);

    // When
        final GraphQLSchema graphQLSchema = swaggerGraphQLSchemaBuilder.swagger(swagger).build();

    // Then
        assertNotNull(graphQLSchema.getQueryType());
    }

    @Test
    public void build_has_to_declare_Query_fields_by_Swagger_path() {
    // Given
        final SwaggerGraphQLSchemaBuilder swaggerGraphQLSchemaBuilder = new SwaggerGraphQLSchemaBuilder();
        final Swagger swagger = new SwaggerParser().read(SWAGGER_LOCATION);

    // When
        final GraphQLSchema graphQLSchema = swaggerGraphQLSchemaBuilder.swagger(swagger).build();

    // Then
        assertEquals(3, graphQLSchema.getQueryType().getFieldDefinitions().size());
        // And
        final GraphQLFieldDefinition queryBooks = graphQLSchema.getQueryType().getFieldDefinition("books");
        assertNotNull(queryBooks);
        assertTrue(queryBooks.getType() instanceof GraphQLList);
        assertEquals("BookDto", ((GraphQLObjectType)((GraphQLList)queryBooks.getType()).getWrappedType()).getName());
        // And
        final GraphQLFieldDefinition queryBooksById = graphQLSchema.getQueryType().getFieldDefinition("booksById");
        assertNotNull(queryBooksById);
        assertTrue(queryBooksById.getType() instanceof GraphQLObjectType);
        assertEquals("BookDto", ((GraphQLObjectType)queryBooksById.getType()).getName());
        assertNotNull(queryBooksById.getArgument("id"));
        assertEquals(GraphQLID, queryBooksById.getArgument("id").getType());
        // And
        assertNotNull(graphQLSchema.getQueryType().getFieldDefinition("booksWithLibraryById"));
    }

    @Test
    public void build_has_to_declare_ObjecType_by_Swagger_Definition() {
    // Given
        final SwaggerGraphQLSchemaBuilder swaggerGraphQLSchemaBuilder = new SwaggerGraphQLSchemaBuilder();
        final Swagger swagger = new SwaggerParser().read(SWAGGER_LOCATION);

    // When
        final GraphQLSchema graphQLSchema = swaggerGraphQLSchemaBuilder.swagger(swagger).build();

    // Then
        /**
         * Refactor creating assertAuthorDto and assertBookDto
         */
        GraphQLObjectType authorDto = graphQLSchema.getObjectType(SWAGGER_DEFINITION_AUTHOR);
        assertNotNull(authorDto);
        assertEquals(3, authorDto.getFieldDefinitions().size());
        assertNotNull(authorDto.getFieldDefinition("id"));
        assertEquals(GraphQLID, authorDto.getFieldDefinition("id").getType());
        assertNotNull(authorDto.getFieldDefinition("firstName"));
        assertEquals(GraphQLString, authorDto.getFieldDefinition("firstName").getType());
        assertNotNull(authorDto.getFieldDefinition("lastName"));
        assertEquals(GraphQLString, authorDto.getFieldDefinition("lastName").getType());
        // And
        GraphQLObjectType bookDto = graphQLSchema.getObjectType(SWAGGER_DEFINITION_BOOK);
        assertNotNull(bookDto);
        assertNotNull(bookDto.getFieldDefinition("id"));
        assertEquals(GraphQLID, bookDto.getFieldDefinition("id").getType());
        assertNotNull(bookDto.getFieldDefinition("name"));
        assertEquals(GraphQLString, bookDto.getFieldDefinition("name").getType());
        assertNotNull(bookDto.getFieldDefinition("pageCount"));
        assertEquals(GraphQLInt, bookDto.getFieldDefinition("pageCount").getType());
        assertNotNull(bookDto.getFieldDefinition("author"));
        assertTrue(bookDto.getFieldDefinition("author").getType() instanceof GraphQLObjectType);
        assertEquals("AuthorDto", ((GraphQLObjectType)bookDto.getFieldDefinition("author").getType()).getName());
    }

    @Test
    public void has_to_create_DataFecher_By_Path() {
    // Given
        final SwaggerGraphQLSchemaBuilder swaggerGraphQLSchemaBuilder = new SwaggerGraphQLSchemaBuilder();
        final Swagger swagger = new SwaggerParser().read(SWAGGER_LOCATION);

    // When
        final GraphQLSchema graphQLSchema = swaggerGraphQLSchemaBuilder.swagger(swagger).build();

    // Then
        /**
         * TODO
         * Define mock for http://localhost:8080/books and http://localhost:8080/books({id}
         * Use https://github.com/square/okhttp/tree/master/mockwebserver
         */
        FieldCoordinates fieldCoordinates = coordinates("Query", "books");
        DataFetcher dataFetcher = graphQLSchema.getCodeRegistry().getDataFetcher(fieldCoordinates, graphQLSchema.getQueryType().getFieldDefinition("books"));
        assertNotNull(dataFetcher);
    }
/*
TODO
// Use with mockwebserver
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://localhost:8081/books/").build();
        Response response = client.newCall(request).execute();
        final String json = response.body().string();
*/
}