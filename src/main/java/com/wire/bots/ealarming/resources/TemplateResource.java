package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.TemplateDAO;
import com.wire.bots.ealarming.model.Template;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api
@Path("/templates")
@Produces(MediaType.APPLICATION_JSON)
public class TemplateResource {
    private final TemplateDAO templateDAO;
    private final AuthValidator validator;

    public TemplateResource(TemplateDAO templateDAO, AuthValidator validator) {
        this.templateDAO = templateDAO;
        this.validator = validator;
    }

    @GET
    @Path("{templateId}")
    @ApiOperation(value = "Get Template by its id")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Template")})
    public Response get(@ApiParam @PathParam("templateId") int templateId) {
        try {
            Template template = templateDAO.get(templateId);
            if (template == null) {
                return Response.
                        status(404).
                        build();
            }

            return Response.
                    ok(template).
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.get(%d): %s", templateId, e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get All Templates")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "List of Templates")})
    public Response getAll() {
        try {
            List<Template> list = templateDAO.select();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("TemplateResource.getAll: %s", e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    @POST
    @ApiOperation(value = "Create new Template")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "New Template")})
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
                    .ok(e)
                    .status(500)
                    .build();
        }
    }
}