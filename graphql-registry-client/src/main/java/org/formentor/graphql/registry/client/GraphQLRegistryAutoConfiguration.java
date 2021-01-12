package org.formentor.graphql.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.formentor.graphql.registry.ServiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@ConfigurationProperties(prefix = "graphql.registry")
@ConditionalOnProperty("graphql.registry.uri")
public class GraphQLRegistryAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(GraphQLRegistryAutoConfiguration.class);
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String uri;
    private final ApplicationContext ctx;
    private final Environment env;

    public GraphQLRegistryAutoConfiguration(ApplicationContext ctx, Environment env) {
        this.ctx = ctx;
        this.env = env;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        final String appName = ctx.getId();
        try {
            register(appName, uri);
        } catch (JsonProcessingException e) {
            LOG.error("ERRORS registering {} in {}", appName, uri, e);
        }
    }

    /**
     * TODO
     *      REFACTOR!!!!!
     * Register the service in Graphql Gateway
     * @param appName
     * @param uri
     * @return
     * @throws JsonProcessingException
     */
    private boolean register(String appName, String uri) throws JsonProcessingException {
        /** Why client and mapper are not created in constructor, because register is executed once */
        final OkHttpClient client = new OkHttpClient();
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setName(appName);
        serviceDto.setUrl("http://" + getServiceHost() + ":" + env.getProperty("local.server.port") + "/v2/api-docs");
        ObjectMapper objectMapper = new ObjectMapper();
        RequestBody body = RequestBody.create(JSON, objectMapper.writeValueAsString(serviceDto));
        Request request = new Request.Builder()
                .url(uri + "/registry")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200 || response.code() == 204) {
                return true;
            } else {
                LOG.error("ERRORS registering {} in {}\n", appName, uri, response.body().string());
                return false;
            }
        } catch (IOException e) {
            LOG.error("ERRORS registering {} in {}", appName, uri, e);
            return false;
        }
    }

    private String getServiceHost() {
        try {
            /*
            // Local address
            InetAddress.getLocalHost().getHostAddress();
            InetAddress.getLocalHost().getHostName();

            // Remote address
            InetAddress.getLoopbackAddress().getHostAddress();
            InetAddress.getLoopbackAddress().getHostName();
            */
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Errors getting the host IP", e);
            return null;
        }
    }
}