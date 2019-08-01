package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.Group;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;


public interface GroupsDAO {
    @SqlQuery("SELECT G.* FROM Template2Group AS T, Groups AS G WHERE T.template_id = :templateId AND T.group_id = G.id")
    @RegisterMapper(GroupsMapper.class)
    List<Group> selectGroups(@Bind("templateId") int templateId);

    @SqlQuery("SELECT * FROM Groups WHERE name ~* :keyword")
    @RegisterMapper(GroupsMapper.class)
    List<Group> search(@Bind("keyword") String keyword);
}
