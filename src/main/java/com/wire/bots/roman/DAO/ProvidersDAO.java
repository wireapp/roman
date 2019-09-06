package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.ProviderMapper;
import com.wire.bots.roman.model.Provider;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.UUID;

public interface ProvidersDAO {
    @SqlUpdate("INSERT INTO Providers (name, id, email, hash, password) " +
            "VALUES (:name, :id, :email, :hash, :password)")
    int insert(@Bind("name") String name,
               @Bind("id") UUID id,
               @Bind("email") String email,
               @Bind("hash") String hash,
               @Bind("password") String password);

    @SqlUpdate("UPDATE Providers SET " +
            "url = :url," +
            "service_auth = :auth, " +
            "service = :serviceId, " +
            "service_name = :serviceName " +
            "WHERE id = :id")
    int update(@Bind("id") UUID id,
               @Bind("url") String url,
               @Bind("auth") String auth,
               @Bind("serviceId") UUID serviceId,
               @Bind("serviceName") String serviceName);

    @SqlUpdate("UPDATE Providers SET url = :url WHERE id = :id")
    void updateUrl(@Bind("id") UUID id,
                   @Bind("url") String url);

    @SqlQuery("SELECT * FROM Providers WHERE email = :email")
    @RegisterMapper(ProviderMapper.class)
    Provider get(@Bind("email") String email);

    @SqlQuery("SELECT * FROM Providers WHERE id = :id")
    @RegisterMapper(ProviderMapper.class)
    Provider get(@Bind("id") UUID id);

    @SqlQuery("SELECT url FROM Providers WHERE service_auth = :auth")
    String getUrl(@Bind("auth") String auth);

    @SqlQuery("SELECT * FROM Providers WHERE service_auth = :auth")
    @RegisterMapper(ProviderMapper.class)
    Provider getByAuth(@Bind("auth") String auth);
}
