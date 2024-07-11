package org.dapr;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dapr.json.Message;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.Metadata;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Path("/produce")
public class Producer {

    String MESSAGE_TTL_IN_SECONDS = "1000";
    String PUBSUB_NAME = "pub-sub";
    String TOPIC_NAME = "dapr";

    private static final Logger LOG = Logger.getLogger(Producer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SECRET_STORE_NAME = "secretstore";

    //SecretService secretService;

    public Producer() {


    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<String> produce(
            @RestHeader("traceparent") String traceparent,
            @RestHeader("tracestate") String tracestate,
            Message message
    ) {

        LOG.infof("traceparent: %s", traceparent);
        LOG.infof("tracestate: %s", tracestate);

        try (DaprClient client = new DaprClientBuilder().build()) {

            Map<String, String> secretFromClient = client.getSecret(SECRET_STORE_NAME, "secret").block();

            LOG.infof("secretFromClient: %s", OBJECT_MAPPER.writeValueAsString(secretFromClient));

            //Response secretFromService = secretService.getSecret();

            //LOG.infof("secretFromService: %s", OBJECT_MAPPER.writeValueAsString(secretFromService));

            message.setValue(secretFromClient != null ? secretFromClient.get("secretFromClient") : null);

            client.publishEvent(
                    PUBSUB_NAME,
                    TOPIC_NAME,
                    message,
                    singletonMap(Metadata.TTL_IN_SECONDS, MESSAGE_TTL_IN_SECONDS)).block();

            LOG.infof("published message: %s", message);

        } catch (Exception e) {
            return ResponseBuilder.create(Response.Status.SERVICE_UNAVAILABLE, e.getMessage()).build();
        }

        return ResponseBuilder.ok("message published", MediaType.APPLICATION_JSON).build();

    }

}
