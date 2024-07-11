package org.dapr;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProducerResourceTest {
    
    @Test
    void testPublishEndpoint() {
        given()
          .when().post("/produce")
          .then()
             .statusCode(415);
    }

}