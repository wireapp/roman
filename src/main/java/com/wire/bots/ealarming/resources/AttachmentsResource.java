package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.AttachmentDAO;
import com.wire.bots.ealarming.model.Attachment;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
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
    public Response insert(@ApiParam(hidden = true) @CookieParam("Authorization") String token,
                           @ApiParam @Valid Attachment attachment) {
        try {
            String subject = validateToken(token);

            int attachmentId = attachmentDAO.insert(attachment.filename,
                    attachment.mimeType,
                    Base64.getDecoder().decode(attachment.data));

            Logger.info("AttachmentsResource.insert: attachment: %d, filename: %s, mime: %s",
                    attachmentId,
                    attachment.filename,
                    attachment.mimeType);

            Attachment result = attachmentDAO.get(attachmentId);

            return Response.
                    ok(result).
                    build();
        } catch (SignatureException e) {
            Logger.warning("AttachmentsResource.insert %s", e);
            return Response.
                    ok(new ErrorMessage("Not authenticated")).
                    status(403).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("AttachmentsResource.insert: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @Path("{attachmentId}")
    @ApiOperation(value = "Get Attachment", response = Attachment.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response get(@ApiParam(hidden = true) @CookieParam("Authorization") String token,
                        @ApiParam @PathParam("attachmentId") int attachmentId) {
        try {
            Attachment attachment = attachmentDAO.get(attachmentId);
            if (attachment == null) {
                return Response.
                        ok(new ErrorMessage("Attachment not found")).
                        status(404).
                        build();
            }

            byte[] data = attachmentDAO.getData(attachmentId);
            attachment.data = Base64.getEncoder().encodeToString(data);

            return Response.
                    ok(attachment).
                    build();
        } catch (SignatureException e) {
            Logger.warning("AttachmentsResource.get(%d) %s", attachmentId, e);
            return Response.
                    ok(new ErrorMessage("Not authenticated")).
                    status(403).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("AssetsResource.get(%d): %s", attachmentId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
