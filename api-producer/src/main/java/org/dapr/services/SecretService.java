package org.dapr.services;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v1.0")
@RegisterRestClient
public interface SecretService {

    @GET
    @Path("/secrets/secretstore/secret")
    Response getSecret();

}