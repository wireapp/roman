package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.DAO.mappers.GroupsMapper;
import com.wire.bots.ealarming.DAO.mappers.UserMapper;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.ealarming.model.User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;


public interface GroupsDAO {
    @SqlQuery("SELECT * FROM Groups WHERE name ~* :keyword ORDER BY name")
    @RegisterMapper(GroupsMapper.class)
    List<Group> search(@Bind("keyword") String keyword);

    @SqlQuery("SELECT * FROM Groups ORDER BY name")
    @RegisterMapper(GroupsMapper.class)
    List<Group> list();

    @SqlQuery("SELECT * FROM Groups WHERE type = :type ORDER BY name")
    @RegisterMapper(GroupsMapper.class)
    List<Group> list(@Bind("type") int type);

    @SqlQuery("SELECT U.* FROM User2Group AS UG, Users AS U WHERE UG.group_id = :groupId AND UG.user_id = U.id ORDER BY U.firstname")
    @RegisterMapper(UserMapper.class)
    List<User> selectUsers(@Bind("groupId") int groupId);

    @SqlQuery("SELECT count(*) AS size FROM User2Group WHERE group_id = :groupId")
    int size(@Bind("groupId") int groupId);
}
