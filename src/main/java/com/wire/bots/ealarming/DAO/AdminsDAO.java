package com.wire.bots.ealarming.DAO;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface AdminsDAO {
    @SqlUpdate("INSERT INTO Admins (email, password) " +
            "VALUES (:email, :password)")
    @GetGeneratedKeys
    int insert(@Bind("email") String email,
               @Bind("password") String password);

    @SqlQuery("SELECT password FROM Admins WHERE email = :email")
    String getHash(@Bind("email") String email);

    @SqlQuery("SELECT id FROM Admins WHERE email = :email")
    int getUserId(@Bind("email") String email);
}
