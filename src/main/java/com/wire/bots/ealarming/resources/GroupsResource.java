package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupsResource {
    private final AuthValidator validator;
    private final GroupsDAO groupsDAO;

    public GroupsResource(GroupsDAO groupsDAO, AuthValidator validator) {
        this.groupsDAO = groupsDAO;
        this.validator = validator;
    }

    @GET
    @Path("{groupId}")
    @ApiOperation(value = "Get Users in the group")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Group")})
    public Response get(@ApiParam @PathParam("groupId") int groupId) {
        try {
            List<User> list = groupsDAO.selectUsers(groupId);
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("GroupsResource.get(%d): %s", groupId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get all groups")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Groups")})
    public Response list() {
        try {
            List<Group> list = groupsDAO.list();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("GroupsResource.list: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

}