package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface UserDAO {
    @SqlQuery("SELECT * FROM Users WHERE firstname ~* :keyword OR " +
            "surname ~* :keyword OR " +
            "department ~* :keyword OR " +
            "location ~* :keyword")
    @RegisterMapper(UserMapper.class)
    List<User> search(@Bind("keyword") String keyword);

    @SqlQuery("SELECT * FROM Users WHERE id = :userId")
    @RegisterMapper(UserMapper.class)
    User get(@Bind("userId") int userId);

    @SqlQuery("SELECT * FROM Users WHERE user_id = :userId")
    @RegisterMapper(UserMapper.class)
    User get(@Bind("userId") UUID userId);

    @SqlUpdate("INSERT INTO Users (firstname, surname, title, department, location, user_id) " +
            "VALUES (:firstname, :surname, :title, :department, :location, :userId)")
    int insertUser(@Bind("userId") UUID userId,
                   @Bind("firstname") String firstname,
                   @Bind("surname") String surname,
                   @Bind("title") String title,
                   @Bind("department") String department,
                   @Bind("location") String location);
}
