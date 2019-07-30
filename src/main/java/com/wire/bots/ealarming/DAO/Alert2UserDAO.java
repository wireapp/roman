package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.Alert2User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface Alert2UserDAO {
    @SqlUpdate("INSERT INTO Alert2User (alert_id, user_id, message_status, escalated, response_id) " +
            "VALUES (:alertId, :userId, :messageStatus, :escalated, :responseId)")
    @GetGeneratedKeys
    int insert(@Bind("alertId") int alertId,
               @Bind("userId") UUID userId,
               @Bind("messageStatus") @Nullable Integer messageStatus,
               @Bind("escalated") @Nullable String escalated,
               @Bind("responseId") @Nullable Integer responseId);


    @SqlQuery("SELECT * FROM Alert2User WHERE alert_id = :alertId")
    @RegisterMapper(Alert2UserMapper.class)
    List<Alert2User> select(@Bind("alertId") int alertId);

}
