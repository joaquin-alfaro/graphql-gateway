import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SwaggerParserTest {

    @Test
    public void shouldBuildSwaggerFromJson() {
        final Swagger swagger = new SwaggerParser().read("src/test/resources/books-swagger.json");

        assertNotNull(swagger);
    }
}