package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.DAO.mappers.Alert2UserMapper;
import com.wire.bots.ealarming.DAO.mappers.ReportMapper;
import com.wire.bots.ealarming.model.Alert2User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface Alert2UserDAO {
    @SqlUpdate("INSERT INTO Alert2User (alert_id, user_id, message_status, message_id, response, created) " +
            "VALUES (:alertId, :userId, :status, :messageId, :response, CURRENT_TIMESTAMP)")
    int insertStatus(@Bind("alertId") int alertId,
                     @Bind("userId") UUID userId,
                     @Bind("status") int status,
                     @Bind("messageId") UUID messageId,
                     @Bind("response") String response);

    @SqlQuery("SELECT alert_id FROM Alert2User WHERE message_id =: messageId")
    int getAlertId(@Bind("messageId") UUID messageId);

    @SqlQuery("SELECT * FROM Alert2User WHERE alert_id = :alertId")
    @RegisterMapper(Alert2UserMapper.class)
    List<Alert2User> listUsers(@Bind("alertId") int alertId);

    @SqlQuery("SELECT count(*) AS count, message_status FROM Alert2User WHERE alert_id = :alertId GROUP BY message_status")
    @RegisterMapper(ReportMapper.class)
    List<_Pair> report(@Bind("alertId") int alertId);

    class _Pair {
        public int type;
        public int count;
    }
}
