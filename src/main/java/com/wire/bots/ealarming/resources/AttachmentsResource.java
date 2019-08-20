package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.AttachmentDAO;
import com.wire.bots.ealarming.model.Attachment;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.postgresql.util.Base64;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("/attachments")
@Produces(MediaType.APPLICATION_JSON)
public class AttachmentsResource {
    private final AttachmentDAO attachmentDAO;
    private final AuthValidator validator;

    public AttachmentsResource(DBI jdbi, AuthValidator validator) {
        this.attachmentDAO = jdbi.onDemand(AttachmentDAO.class);

        this.validator = validator;
    }

    @POST
    @ApiOperation(value = "Upload Attachment", response = Attachment.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response insert(@ApiParam @Valid Attachment attachment) {
        try {
            int attachmentId = attachmentDAO.insert(attachment.filename,
                    attachment.mimeType,
                    Base64.decode(attachment.data));

            Logger.info("AssetsResource.insert: attachment: %d, filename: %s, mime: %s",
                    attachmentId,
                    attachment.filename,
                    attachment.mimeType);

            Attachment result = attachmentDAO.get(attachmentId);
            result.data = null;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("AssetsResource.insert: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
