package cc.spherix.internal.dao;

import cc.spherix.externalCommandBroker.util.Logger;
import cc.spherix.internal.domain.Token;
import cc.spherix.internal.service.DataSourceService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TokenDao implements BaseDao<Token, Long> {

    private static TokenDao instance;
    private final DataSourceService service;

    private TokenDao(DataSourceService service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<Optional<Token>> getById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT id, username, token, disabled FROM tokens WHERE id = ?";
            try (Connection connection = service.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    if (resultSet.next()) {
                        final Long tokenId = resultSet.getLong("id");
                        final String username = resultSet.getString("username");
                        final String token = resultSet.getString("token");
                        final boolean disabled = resultSet.getBoolean("disabled");

                        return Optional.of(new Token(tokenId, username, token, disabled));
                    }
                }
            } catch (SQLException e) {
                Logger.warn("Error on reading token entity by id {}", id);
                Logger.error("Error on reading token entity", e);
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<List<Token>> getAllTokens() {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT id, username, token, disabled FROM tokens WHERE disabled = false";
            final List<Token> tokens = new ArrayList<>();

            try (Connection connection = service.getConnection();
                 Statement preparedStatement = connection.createStatement();
                 ResultSet resultSet = preparedStatement.executeQuery(sql)) {

                while (resultSet.next()) {
                    final Long tokenId = resultSet.getLong("id");
                    final String username = resultSet.getString("username");
                    final String token = resultSet.getString("token");
                    final boolean disabled = resultSet.getBoolean("disabled");

                    tokens.add(new Token(tokenId, username, token, disabled));
                }
            } catch (SQLException e) {
                Logger.error("Error on reading token entities", e);
            }
            return tokens;
        });
    }

    @Override
    public void save(Token entity) {
        // not implemented
    }

    @Override
    public void delete(Token entity) {
        // not implemented
    }

    public static TokenDao init(DataSourceService service) {
        if (instance == null) {
            instance = new TokenDao(service);
        }
        return instance;
    }

    public static TokenDao getInstance() {
        return instance;
    }
}
