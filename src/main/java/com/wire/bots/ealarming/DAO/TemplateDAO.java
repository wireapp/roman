package com.wire.bots.ealarming.DAO;

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
    @SqlUpdate("INSERT INTO Template (title, message, category, severity, contact, responses, created) " +
            "VALUES (:title, :message, :category, :severity, :contact, :responses, CURRENT_TIMESTAMP)")
    @GetGeneratedKeys
    int insert(@Bind("title") String title,
               @Bind("message") String message,
               @Bind("category") String category,
               @Bind("severity") int severity,
               @Bind("contact") @Nullable UUID contact,
               @Bind("responses") String responses);

    @SqlQuery("SELECT * FROM Template WHERE id = :id")
    @RegisterMapper(TemplateMapper.class)
    Template get(@Bind("id") int id);

    @SqlQuery("SELECT * FROM Template ORDER BY created DESC")
    @RegisterMapper(TemplateMapper.class)
    List<Template> select();

    @SqlUpdate("INSERT INTO Template2Group (template_id, group_id) VALUES (:templateId, :groupId)")
    int putGroup(@Bind("templateId") int templateId,
                 @Bind("groupId") int groupId);

}
