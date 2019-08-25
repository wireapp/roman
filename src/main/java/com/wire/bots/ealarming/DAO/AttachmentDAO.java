package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.DAO.mappers.AttachmentMapper;
import com.wire.bots.ealarming.model.Attachment;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

public interface AttachmentDAO {
    @SqlUpdate("INSERT INTO Attachment (filename, mime_type, data) " +
            "VALUES (:filename, :mimeType, :data)")
    @GetGeneratedKeys
    int insert(@Bind("filename") String filename,
               @Bind("mimeType") String mimeType,
               @Bind("data") byte[] data);

    @SqlQuery("SELECT * FROM Attachment WHERE id = :id")
    @RegisterMapper(AttachmentMapper.class)
    Attachment get(@Bind("id") int id);

    @SqlQuery("SELECT data FROM Attachment WHERE id = :id")
    byte[] getData(@Bind("id") int id);
}
