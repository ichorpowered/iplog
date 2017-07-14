/*
 * This file is part of IPLog, licensed under the MIT License.
 *
 * Copyright (c) 2017 Meronat <http://meronat.com>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ichorpowered.iplog;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Storage {

    private SqlService sql;

    public Storage() throws SQLException {
        createTables();
    }

    private Connection getConnection() throws SQLException {
        if (this.sql == null) {
            Optional<SqlService> optionalSql = Sponge.getServiceManager().provide(SqlService.class);

            if (optionalSql.isPresent()) {
                this.sql = optionalSql.get();
            } else {
                throw new SQLException("Sponge SQL service is missing.");
            }
        }

        return this.sql.getDataSource("jdbc:h2:" + IPLog.getPlugin().getParentPath().toAbsolutePath().toString()
                + "/storage.db").getConnection();
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS REGISTRY("
                + " IP VARCHAR(45),"
                + " ID CHAR(36),"
                + " INSTANT DATETIME,"
                + " PRIMARY KEY(IP, ID))").execute();
        }
    }

    public boolean isPresent(InetAddress ip, UUID uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM REGISTRY WHERE IP = ? AND ID = ?");
        ) {
            ps.setString(1, ip.getHostAddress());
            ps.setString(2, uuid.toString());

            return ps.executeQuery().next();
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to verify existence of player");
            e.printStackTrace();
        }

        return false;
    }

    public void addConnection(InetAddress ip, UUID uuid, LocalDateTime time) {
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO REGISTRY(IP, ID, INSTANT) VALUES (?, ?, ?)");
        ) {
            ps.setString(1, ip.getHostAddress());
            ps.setString(2, uuid.toString());
            ps.setTimestamp(3, Timestamp.valueOf(time));

            ps.execute();
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to create new connection.");
            e.printStackTrace();
        }
    }

    public void updateConnection(InetAddress ip, UUID uuid, LocalDateTime time) {
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE REGISTRY SET INSTANT = ? WHERE IP = ? AND ID = ?");
        ) {
            ps.setTimestamp(1, Timestamp.valueOf(time));
            ps.setString(2, ip.getHostAddress());
            ps.setString(3, uuid.toString());

            ps.execute();
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to update old connection.");
            e.printStackTrace();
        }
    }

    public void purgeConnection(InetAddress ip, UUID uuid) {
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM REGISTRY WHERE IP = ? AND ID = ?");
        ) {
            ps.setString(1, ip.getHostAddress());
            ps.setString(2, uuid.toString());

            ps.execute();
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to purge connection.");
            e.printStackTrace();
        }
    }

    public Set<UUID> getAliases(UUID uuid) {
        final Set<UUID> aliases = new HashSet<>();

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT(REG.ID) FROM REGISTRY JOIN REGISTRY REG ON (REGISTRY.IP = REG.IP) WHERE REGISTRY.ID = ?");
        ) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    aliases.add(UUID.fromString(rs.getString(1)));
                }
            }
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to get all possible aliases of a player from storage.");
            e.printStackTrace();
        }

        return aliases;
    }

    public Set<UUID> getPlayers(InetAddress ip) {
        final Set<UUID> players = new HashSet<>();

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT ID FROM REGISTRY WHERE IP = ?");
        ) {
            ps.setString(1, ip.getHostAddress());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    players.add(UUID.fromString(rs.getString(1)));
                }
            }
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to get all players connected to this ip address.");
            e.printStackTrace();
        }

        return players;
    }

    public Set<String> getAddresses(UUID uuid) {
        final Set<String> addresses = new HashSet<>();

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT IP FROM REGISTRY WHERE ID = ?");
        ) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {

            IPLog.getPlugin().getLogger().error("Failed to get all ip addresses connected to this uuid.");
            e.printStackTrace();

        }

        return addresses;
    }

    public Map<String, LocalDateTime> getAddressesAndTime(UUID uuid) {
        final Map<String, LocalDateTime> data = new HashMap<>();

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT IP, INSTANT FROM REGISTRY WHERE ID = ? ORDER BY INSTANT");
        ) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(rs.getString(1), rs.getTimestamp(2).toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to get all ip addresses and dates connected to this uuid.");
            e.printStackTrace();
        }

        return data;
    }

    public Map<UUID, LocalDateTime> getPlayersAndTime(InetAddress ip) {
        final Map<UUID, LocalDateTime> data = new HashMap<>();

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT ID, INSTANT FROM REGISTRY WHERE IP = ? ORDER BY INSTANT")
        ) {
            ps.setString(1, ip.getHostAddress());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.put(UUID.fromString(rs.getString(1)), rs.getTimestamp(2).toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            IPLog.getPlugin().getLogger().error("Failed to get all uuids and dates connected to this ip address.");
            e.printStackTrace();
        }

        return data;
    }

}
