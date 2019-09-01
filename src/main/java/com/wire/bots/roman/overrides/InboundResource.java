package com.wire.bots.roman.resources;


import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.server.resources.MessageResource;
import io.swagger.annotations.Api;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/bots/{bot}/messages")
public class InboundResource extends MessageResource {
    private final ProvidersDAO providersDAO;

    public InboundResource(MessageHandlerBase handler, ClientRepo repo, DBI jdbi) {
        super(handler, null, repo);
        this.providersDAO = jdbi.onDemand(ProvidersDAO.class);
    }

    protected boolean isValid(String auth) {
        String token = auth.replace("Bearer", "").trim();
        String url = providersDAO.getUrl(token);
        return url != null;
    }
}
