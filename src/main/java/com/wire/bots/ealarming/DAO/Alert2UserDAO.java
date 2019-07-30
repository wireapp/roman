package com.wire.bots.ealarming.DAO;

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
            "VALUES (:alertId, :userId)")
    int insertUser(@Bind("alertId") int alertId,
                   @Bind("userId") UUID userId);
}
