package org.dapr;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.dapr.client.domain.State;

import org.dapr.json.Message;
import org.jboss.logging.Logger;
import io.dapr.Topic;
import org.jboss.resteasy.reactive.RestHeader;
import reactor.core.publisher.Mono;

@Path("/consume")
public class Consumer {

    private static final Logger LOG = Logger.getLogger(Consumer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String STATE_STORE_NAME = "statestore";

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.WILDCARD)
    @Topic(name = "dapr", pubsubName = "pub-sub")
    public Message consume(
            @RestHeader("traceparent") String traceparent,
            @RestHeader("tracestate") String tracestate,
            CloudEvent<Message> content
    ) {

        LOG.infof("traceparent: %s", traceparent);
        LOG.infof("tracestate: %s", tracestate);

        try (DaprClient client = new DaprClientBuilder().build()) {

            LOG.infof("received message: %s", OBJECT_MAPPER.writeValueAsString(content));
            client.saveState(STATE_STORE_NAME, "STATE_" + content.getData().getKey(), content.getId()).block();

            Mono<State<String>> result = client.getState(STATE_STORE_NAME, "STATE_" + content.getData().getKey(), String.class);
            LOG.infof("from redis: %s", result.block());

            return content.getData();

        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

    }

}