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

    @SqlQuery("SELECT * FROM Alert2User WHERE alert_id = :alertId")
    @RegisterMapper(Alert2UserMapper.class)
    List<Alert2User> selectUsers(@Bind("alertId") int alertId);

    @SqlUpdate("INSERT INTO Alert2User (alert_id, user_id) " +
            "VALUES (:alertId, :userId) ON CONFLICT(alert_id,user_id) DO NOTHING")
    int insertUser(@Bind("alertId") int alertId,
                   @Bind("userId") UUID userId);

    @SqlUpdate("UPDATE Alert2User SET message_status = :status, message_id = :messageId WHERE alert_id = :alertId AND user_id = :userId")
    int insertStatus(@Bind("alertId") int alertId,
                     @Bind("userId") UUID userId,
                     @Bind("messageId") UUID messageId,
                     @Bind("status") int status);

    @SqlQuery("SELECT * FROM Alert2User WHERE user_id = :userId AND message_id =: messageId")
    @RegisterMapper(Alert2UserMapper.class)
    Alert2User getStatus(@Bind("userId") UUID userId,
                         @Bind("messageId") UUID messageId);

    @SqlUpdate("UPDATE Alert2User SET message_status = :status, response = :response WHERE message_id = :messageId AND user_id = :userId")
    int updateStatus(@Bind("userId") UUID userId,
                     @Bind("messageId") UUID messageId,
                     @Bind("response") String response,
                     @Bind("status") int status);

    @SqlQuery("SELECT count(*) AS count, message_status FROM Alert2User WHERE alert_id = :alertId GROUP BY message_status")
    @RegisterMapper(ReportMapper.class)
    List<_Pair> report(@Bind("alertId") int alertId);

    class _Pair {
        public int type;
        public int count;
    }
}
