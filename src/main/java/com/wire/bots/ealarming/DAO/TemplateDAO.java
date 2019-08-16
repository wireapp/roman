package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.DAO.mappers.TemplateMapper;
import com.wire.bots.ealarming.model.Template;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface TemplateDAO {
    @SqlUpdate("INSERT INTO Template (title, message, severity, contact, created) " +
            "VALUES (:title, :message, :severity, :contact, CURRENT_TIMESTAMP)")
    @GetGeneratedKeys
    int insert(@Bind("title") String title,
               @Bind("message") String message,
               @Bind("severity") int severity,
               @Bind("contact") @Nullable UUID contact);

    @SqlUpdate("UPDATE Template SET title = :title, message = :message, severity = :severity," +
            " contact = :contact WHERE id = :id")
    int update(@Bind("id") int id,
               @Bind("title") String title,
               @Bind("message") String message,
               @Bind("severity") int severity,
               @Bind("contact") UUID contact);

    @SqlUpdate("DELETE FROM Template WHERE id = :id")
    int delete(@Bind("id") int id);

    @SqlQuery("SELECT * FROM Template WHERE id = :id")
    @RegisterMapper(TemplateMapper.class)
    Template get(@Bind("id") int id);

    @SqlQuery("SELECT * FROM Template ORDER BY created DESC")
    @RegisterMapper(TemplateMapper.class)
    List<Template> select();

    @SqlUpdate("INSERT INTO Template2Response (template_id, response) VALUES (:templateId, :response)")
    int addResponse(@Bind("templateId") int templateId,
                    @Bind("response") String response);

    @SqlQuery("SELECT response FROM Template2Response WHERE template_id = :templateId")
    List<String> selectResponses(@Bind("templateId") int templateId);

    @SqlUpdate("DELETE FROM Template2Response WHERE template_id = :templateId AND response = :response")
    int removeResponse(@Bind("templateId") int templateId,
                       @Bind("response") String response);

    @SqlUpdate("DELETE FROM Template2Response WHERE template_id = :templateId")
    int removeAllResponses(@Bind("templateId") int templateId);
}
