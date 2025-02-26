package cc.spherix.internal.dao;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface BaseDao<T, S> {

    CompletableFuture<Optional<T>> getById(S id) throws SQLException;

    void save(T entity);

    void delete(T entity);
}
