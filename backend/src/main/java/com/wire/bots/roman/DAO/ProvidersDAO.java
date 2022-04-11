package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.ProviderMapper;
import com.wire.bots.roman.model.Provider;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

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
            "command_prefix = :command_prefix, " +
            "service_name = :serviceName " +
            "WHERE id = :id")
    int update(@Bind("id") UUID id,
               @Bind("url") String url,
               @Bind("auth") String auth,
               @Bind("serviceId") UUID serviceId,
               @Bind("serviceName") String serviceName,
               @Bind("command_prefix") String command_prefix);

    @SqlUpdate("UPDATE Providers SET " +
            "url = null," +
            "service_auth = null, " +
            "service = null, " +
            "command_prefix = null, " +
            "service_name = null " +
            "WHERE id = :id")
    int deleteService(@Bind("id") UUID providerId);

    @SqlUpdate("UPDATE Providers SET url = :url WHERE id = :id")
    int updateUrl(@Bind("id") UUID id,
                  @Bind("url") String url);

    @SqlUpdate("UPDATE Providers SET service_name = :name WHERE id = :id")
    int updateServiceName(@Bind("id") UUID id,
                          @Bind("name") String name);

    @SqlUpdate("UPDATE Providers SET command_prefix = :command_prefix WHERE id = :id")
    int updateServicePrefix(@Bind("id") UUID id,
                            @Bind("command_prefix") String command_prefix);

    @SqlQuery("SELECT * FROM Providers WHERE email = :email")
    @RegisterColumnMapper(ProviderMapper.class)
    Provider get(@Bind("email") String email);

    @SqlQuery("SELECT * FROM Providers")
    @RegisterColumnMapper(ProviderMapper.class)
    List<Provider> selectAll();

    @SqlQuery("SELECT * FROM Providers WHERE id = :id")
    @RegisterColumnMapper(ProviderMapper.class)
    Provider get(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM Providers WHERE service_auth = :auth")
    @RegisterColumnMapper(ProviderMapper.class)
    Provider getByAuth(@Bind("auth") String auth);
}
