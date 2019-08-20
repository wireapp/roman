package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.DAO.mappers.AlertMapper;
import com.wire.bots.ealarming.DAO.mappers.GroupsMapper;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.Group;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import javax.annotation.Nullable;
import java.util.List;

public interface AlertDAO {
    @SqlUpdate("INSERT INTO Alert (title, message, severity, attachment, starting, ending, status, created) " +
            "VALUES (:title, :message, :severity, :attachment, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP)")
    @GetGeneratedKeys
    int insert(@Bind("title") String title,
               @Bind("message") String message,
               @Bind("severity") int severity,
               @Bind("attachment") @Nullable Integer attachment);

    @SqlQuery("SELECT * FROM Alert WHERE id = :id")
    @RegisterMapper(AlertMapper.class)
    Alert get(@Bind("id") int id);

    @SqlQuery("SELECT * FROM Alert ORDER BY created DESC")
    @RegisterMapper(AlertMapper.class)
    List<Alert> list();

    @SqlUpdate("INSERT INTO Alert2Group (alert_id, group_id) VALUES (:alertId, :groupId)")
    int putGroup(@Bind("alertId") int alertId,
                 @Bind("groupId") int groupId);

    @SqlQuery("SELECT G.* FROM Alert2Group AS A, Groups AS G WHERE A.alert_id = :alertId AND A.group_id = G.id")
    @RegisterMapper(GroupsMapper.class)
    List<Group> selectGroups(@Bind("alertId") int alertId);

    @SqlUpdate("INSERT INTO Alert2Response (alert_id, response) VALUES (:alertId, :response)")
    int addResponse(@Bind("alertId") int alertId,
                    @Bind("response") String response);

    @SqlQuery("SELECT response FROM Alert2Response WHERE alert_id = :alertId")
    List<String> selectResponses(@Bind("alertId") int alertId);

    @SqlUpdate("DELETE FROM Alert2Response WHERE alert_id = :alertId")
    int removeAllResponses(@Bind("alertId") int alertId);

}
