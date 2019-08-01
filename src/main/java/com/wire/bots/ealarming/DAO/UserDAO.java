package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface UserDAO {
    @SqlQuery("SELECT * FROM Users WHERE firstname ~* :keyword OR " +
            "surname ~* :keyword OR " +
            "department ~* :keyword OR " +
            "location ~* :keyword")
    @RegisterMapper(UserMapper.class)
    List<User> search(@Bind("keyword") String keyword);

}
