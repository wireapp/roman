package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.Alert;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface AlertDAO {
    @SqlUpdate("INSERT INTO Alert (title, message, category, severity, creator, contact, starting, ending, status, responses, created) " +
            "VALUES (:title, :message, :category, :severity, :creator, :contact, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :status, :responses, CURRENT_TIMESTAMP)")
    @GetGeneratedKeys
    int insert(@Bind("title") String title,
               @Bind("message") String message,
               @Bind("category") String category,
               @Bind("severity") int severity,
               @Bind("creator") UUID creator,
               @Bind("contact") @Nullable UUID contact,
               @Bind("starting") String starting,
               @Bind("ending") String ending,
               @Bind("status") int status,
               @Bind("responses") String responses);

    @SqlQuery("SELECT * FROM Alert WHERE id = :id")
    @RegisterMapper(AlertResultSetMapper.class)
    Alert get(@Bind("id") int id);

    @SqlQuery("SELECT * FROM Alert ORDER BY created DESC")
    @RegisterMapper(AlertResultSetMapper.class)
    List<Alert> list();

}
