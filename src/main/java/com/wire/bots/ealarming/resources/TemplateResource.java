package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.TemplateDAO;
import com.wire.bots.ealarming.model.Template;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

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

    public TemplateResource(DBI jdbi) {
        this.templateDAO = jdbi.onDemand(TemplateDAO.class);
        ;
    }

    @GET
    @Path("{templateId}")
    @ApiOperation(value = "Get Template by templateId", response = Template.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class),
            @ApiResponse(code = 404, message = "Template not found", response = ErrorMessage.class)})
    public Response get(@ApiParam @PathParam("templateId") int templateId) {
        try {
            Template template = getTemplate(templateId);
            if (template == null) {
                return Response.
                        ok(new ErrorMessage("Template not found")).
                        status(404).
                        build();
            }

            return Response.
                    ok(template).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("TemplateResource.get(%d): %s", templateId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get All Templates", response = Template.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response getAll() {
        try {
            List<Template> templates = templateDAO.select();
            for (Template template : templates) {
                template.responses = templateDAO.selectResponses(template.id);
            }
            return Response.
                    ok(templates).
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
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response insert(@ApiParam @Valid Template template) {
        try {
            int templateId = templateDAO.insert(template.title,
                    template.message,
                    template.severity,
                    template.contact);

            for (String response : template.responses) {
                int insert = templateDAO.addResponse(templateId, response);
                if (insert == 0)
                    Logger.warning("TemplateResource.insert: addResponse: template: %d, response: %s. insert: %s",
                            templateId, response, insert);
            }

            Template ret = getTemplate(templateId);

            Logger.info("TemplateResource.insert: template: %d, title: %s", templateId, template.title);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("TemplateResource.insert: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @Path("{templateId}")
    @ApiOperation(value = "Update template", code = 201, response = Template.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class),
            @ApiResponse(code = 404, message = "Template not found", response = ErrorMessage.class)})
    public Response update(@ApiParam @PathParam("templateId") int templateId,
                           @ApiParam @Valid Template template) {
        try {
            if (null == getTemplate(templateId)) {
                return Response.
                        ok(new ErrorMessage("Template not found")).
                        status(404).
                        build();
            }

            int update = templateDAO.update(templateId,
                    template.title,
                    template.message,
                    template.severity,
                    template.contact);

            templateDAO.removeAllResponses(templateId);

            for (String response : template.responses) {
                templateDAO.addResponse(templateId, response);
            }

            Template ret = getTemplate(templateId);
            return Response.
                    ok(ret).
                    status(201).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("TemplateResource.update(%d): %s", templateId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @DELETE
    @Path("{templateId}")
    @ApiOperation(value = "Delete template")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class),
            @ApiResponse(code = 404, message = "Template not found", response = ErrorMessage.class)})
    public Response delete(@ApiParam @PathParam("templateId") int templateId) {
        try {
            int del = templateDAO.delete(templateId);
            templateDAO.removeAllResponses(templateId);

            if (del == 0) {
                return Response.
                        ok(new ErrorMessage("Template not found")).
                        status(404).
                        build();
            }

            return Response.
                    ok().
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("TemplateResource.delete(%d): %s", templateId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private Template getTemplate(int templateId) {
        Template ret = templateDAO.get(templateId);
        if (ret == null)
            return null;

        ret.responses = templateDAO.selectResponses(templateId);
        return ret;
    }
}