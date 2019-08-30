package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.AttachmentDAO;
import com.wire.bots.ealarming.model.Attachment;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.JwtException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;

import static com.wire.bots.ealarming.Tools.validateToken;

@Api
@Path("/attachments")
@Produces(MediaType.APPLICATION_JSON)
public class AttachmentsResource {
    private final AttachmentDAO attachmentDAO;

    public AttachmentsResource(DBI jdbi) {
        this.attachmentDAO = jdbi.onDemand(AttachmentDAO.class);

    }

    @POST
    @ApiOperation(value = "Upload Attachment", response = Attachment.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response insert(@ApiParam(hidden = true) @NotNull @CookieParam("Authorization") String token,
                           @ApiParam @Valid Attachment attachment) {
        try {
            String subject = validateToken(token);

            int attachmentId = attachmentDAO.insert(attachment.filename,
                    attachment.mimeType,
                    Base64.getDecoder().decode(attachment.data));

            Logger.info("AttachmentsResource.insert: user: %s, attachment: %d, filename: %s, mime: %s",
                    subject,
                    attachmentId,
                    attachment.filename,
                    attachment.mimeType);

            Attachment result = attachmentDAO.get(attachmentId);

            return Response.
                    ok(result).
                    build();
        } catch (JwtException e) {
            Logger.warning("AttachmentsResource.insert %s", e);
            return Response.
                    ok(new ErrorMessage(e.getMessage())).
                    status(403).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("AttachmentsResource.insert: %s", e);
            return Response
                    .status(500)
                    .build();
        }
    }

    @GET
    @Path("{attachmentId}")
    @ApiOperation(value = "Get Attachment", response = Attachment.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response get(@ApiParam(hidden = true) @NotNull @CookieParam("Authorization") String token,
                        @ApiParam @PathParam("attachmentId") int attachmentId) {
        try {
            String subject = validateToken(token);

            Attachment attachment = attachmentDAO.get(attachmentId);
            if (attachment == null) {
                return Response.
                        ok(new ErrorMessage("Attachment not found")).
                        status(404).
                        build();
            }

            byte[] data = attachmentDAO.getData(attachmentId);
            attachment.data = Base64.getEncoder().encodeToString(data);

            Logger.info("AttachmentsResource.get: user: %s, attachment: %d, filename: %s, mime: %s",
                    subject,
                    attachmentId,
                    attachment.filename,
                    attachment.mimeType);

            return Response.
                    ok(attachment).
                    build();
        } catch (JwtException e) {
            Logger.warning("AttachmentsResource.get(%d) %s", attachmentId, e);
            return Response.
                    ok(new ErrorMessage(e.getMessage())).
                    status(403).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("AssetsResource.get(%d): %s", attachmentId, e);
            return Response
                    .status(500)
                    .build();
        }
    }
}
