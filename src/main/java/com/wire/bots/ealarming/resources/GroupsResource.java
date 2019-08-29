package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.ealarming.model.Result;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public class GroupsResource {
    private final GroupsDAO groupsDAO;

    public GroupsResource(DBI jdbi) {

        this.groupsDAO = jdbi.onDemand(GroupsDAO.class);
    }

    @GET
    @Path("{groupId}")
    @ApiOperation(value = "Get Users in the group", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Group")})
    public Response get(@ApiParam @PathParam("groupId") int groupId) {
        try {
            Result<User> result = new Result<>();
            result.items = groupsDAO.selectUsers(groupId);
            result.size = result.items.size();
            result.page = 1;
            return Response.
                    ok(result).
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
    @ApiOperation(value = "Get all groups", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Groups")})
    public Response list(@ApiParam @QueryParam("type") Integer type) {
        try {
            Result<Group> result = new Result<>();

            result.items = type == null
                    ? groupsDAO.list()
                    : groupsDAO.list(type);
            result.size = result.items.size();
            result.page = 1;

            for (Group group : result.items) {
                group.size = groupsDAO.size(group.id);
            }
            return Response.
                    ok(result).
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