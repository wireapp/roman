package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.ealarming.model.SearchResult;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private final GroupsDAO groupsDAO;
    private final AuthValidator validator;
    private final UserDAO userDAO;

    public SearchResource(UserDAO userDAO, GroupsDAO groupsDAO, AuthValidator validator) {
        this.userDAO = userDAO;
        this.groupsDAO = groupsDAO;
        this.validator = validator;
    }

    @GET
    @ApiOperation(value = "Search users by the keyword", response = SearchResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response search(@ApiParam @QueryParam("q") String keyword) {
        try {
            SearchResult result = new SearchResult();
            result.users = userDAO.search(keyword);
            result.groups = groupsDAO.search(keyword);

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("SearchResource.search?q=%s : %s", keyword, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}