package com.example.cinema_app_lab25_lab26;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Punctul unic de acces la baza de date. Datele de conectare vin din db.properties. */
public final class Database {

    private static Properties props;

    private Database() { }

    private static synchronized Properties props() {
        if (props == null) {
            Properties p = new Properties();
            try (InputStream in = Database.class.getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException(
                            "Lipsește db.properties din resources/md/tap/lab25/");
                }
                p.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Nu pot citi db.properties", e);
            }
            props = p;
        }
        return props;
    }

    /** Deschide o conexiune nouă; apelantul o închide (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        Properties p = props();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password"));
    }

    /** Verificare rapidă: pornește baza? Folosită pentru alegerea providerului (rezerva în memorie). */
    public static boolean isAvailable() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (SQLException | RuntimeException e) {
            return false;
        }
    }
}