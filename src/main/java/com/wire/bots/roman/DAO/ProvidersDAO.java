package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.ProviderMapper;
import com.wire.bots.roman.model.Provider;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface ProvidersDAO {
    @SqlUpdate("INSERT INTO Providers (name, id, email, hash, password) " +
            " VALUES (:name, :id, :email, :hash, :password) " +
            " ON CONFLICT (email) DO UPDATE SET " +
            " name = EXCLUDED.name, " +
            " hash = EXCLUDED.hash, " +
            " password = EXCLUDED.password, " +
            " id = EXCLUDED.id ")
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
    int updateUrl(@Bind("id") UUID id,
                  @Bind("url") String url);

    @SqlUpdate("UPDATE Providers SET service_name = :name WHERE id = :id")
    int updateServiceName(@Bind("id") UUID id,
                          @Bind("name") String name);

    @SqlQuery("SELECT * FROM Providers WHERE email = :email")
    @RegisterMapper(ProviderMapper.class)
    Provider get(@Bind("email") String email);

    @SqlQuery("SELECT * FROM Providers")
    @RegisterMapper(ProviderMapper.class)
    List<Provider> selectAll();

    @SqlQuery("SELECT * FROM Providers WHERE id = :id")
    @RegisterMapper(ProviderMapper.class)
    Provider get(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM Providers WHERE service_auth = :auth")
    @RegisterMapper(ProviderMapper.class)
    Provider getByAuth(@Bind("auth") String auth);
}
