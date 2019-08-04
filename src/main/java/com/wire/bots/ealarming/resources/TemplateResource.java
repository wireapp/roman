package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.DAO.TemplateDAO;
import com.wire.bots.ealarming.model.Template;
import com.wire.bots.ealarming.model.TemplateResult;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api
@Path("/templates")
@Produces(MediaType.APPLICATION_JSON)
public class TemplateResource {
    private final TemplateDAO templateDAO;
    private final GroupsDAO groupsDAO;
    private final AuthValidator validator;

    public TemplateResource(TemplateDAO templateDAO, GroupsDAO groupsDAO, AuthValidator validator) {
        this.templateDAO = templateDAO;
        this.groupsDAO = groupsDAO;
        this.validator = validator;
    }

    @GET
    @Path("{templateId}")
    @ApiOperation(value = "Get Template by its id", response = TemplateResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 404, message = "Template not found", response = ErrorMessage.class)})
    public Response get(@ApiParam @PathParam("templateId") int templateId) {
        try {
            TemplateResult result = new TemplateResult();
            result.template = templateDAO.get(templateId);
            if (result.template == null) {
                return Response.
                        status(404).
                        build();
            }

            result.groups = groupsDAO.selectGroups(templateId);

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.get(%d): %s", templateId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get All Templates", response = Template.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response getAll() {
        try {
            List<Template> list = templateDAO.select();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.getAll: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @POST
    @ApiOperation(value = "Create new Template", response = Template.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response post(@ApiParam @Valid Template template) {
        try {
            int id = templateDAO.insert(template.title,
                    template.message,
                    template.category,
                    template.severity,
                    template.contact,
                    template.responses);

            Template ret = templateDAO.get(id);
            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.post: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @Path("{templateId}")
    @ApiOperation(value = "Add Groups for this Template")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response putGroups(@ApiParam @PathParam("templateId") int templateId,
                              @ApiParam @Valid ArrayList<Integer> groups) {
        try {
            for (Integer groupId : groups) {
                templateDAO.putGroup(templateId, groupId);
            }
            return Response.
                    ok().
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.putGroups: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}