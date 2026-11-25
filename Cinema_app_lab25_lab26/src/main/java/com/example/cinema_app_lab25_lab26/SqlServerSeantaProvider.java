package com.example.cinema_app_lab25_lab26;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Implementare pe SQL Server: PreparedStatement, try-with-resources, fără concatenare de SQL. */
public class SqlServerSeantaProvider implements SeantaProvider {

    private static final String SELECT_ALL =
            "SELECT Id, Film, Sala, Ora, Pret, LocuriLibere, Gen, DurataMin, Rating, Descriere, Poster "
                    + "FROM dbo.Seante ORDER BY Id";

    private static final String INSERT =
            "INSERT INTO dbo.Seante (Film, Sala, Ora, Pret, LocuriLibere, Gen, DurataMin, Rating, Descriere, Poster) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE dbo.Seante SET Film = ?, Sala = ?, Ora = ?, Pret = ?, LocuriLibere = ?, "
                    + "Gen = ?, DurataMin = ?, Rating = ?, Descriere = ?, Poster = ? WHERE Id = ?";

    private static final String DELETE = "DELETE FROM dbo.Seante WHERE Id = ?";

    @Override
    public List<Seanta> findAll() {
        List<Seanta> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Seanta(
                        rs.getInt("Id"),
                        rs.getString("Film"),
                        rs.getString("Sala"),
                        rs.getString("Ora").trim(),
                        rs.getDouble("Pret"),
                        rs.getInt("LocuriLibere"),
                        rs.getString("Gen"),
                        rs.getInt("DurataMin"),
                        rs.getDouble("Rating"),
                        nullToEmpty(rs.getString("Descriere")),
                        nullToEmpty(rs.getString("Poster"))));
            }
        } catch (SQLException e) {
            throw translate(e, "Nu am putut citi seanțele din baza de date.");
        }
        return result;
    }

    @Override
    public Seanta save(Seanta s) {
        try (Connection c = Database.getConnection()) {
            if (s.getId() == 0) {                                   // INSERT
                try (PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    bind(ps, s);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            s.setId(keys.getInt(1));                // id-ul generat de IDENTITY
                        }
                    }
                }
            } else {                                                // UPDATE
                try (PreparedStatement ps = c.prepareStatement(UPDATE)) {
                    int next = bind(ps, s);
                    ps.setInt(next, s.getId());
                    ps.executeUpdate();
                }
            }
            return s;
        } catch (SQLException e) {
            throw translate(e, "Nu am putut salva seanța.");
        }
    }

    @Override
    public void delete(int id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw translate(e, "Nu am putut șterge seanța.");
        }
    }

    /** Leagă cei 10 parametri comuni INSERT/UPDATE; întoarce următorul index liber. */
    private int bind(PreparedStatement ps, Seanta s) throws SQLException {
        int i = 1;
        ps.setString(i++, s.getFilm());
        ps.setString(i++, s.getSala());
        ps.setString(i++, s.getOra());
        ps.setDouble(i++, s.getPret());
        ps.setInt(i++, s.getLocuriLibere());
        ps.setString(i++, s.getGen());
        ps.setInt(i++, s.getDurataMin());
        ps.setDouble(i++, s.getRating());
        ps.setString(i++, s.getDescriere());
        ps.setString(i++, s.getPoster());
        return i;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    /** Transformă erorile SQL Server în mesaje clare (nivelul 3 al validării: plasă de siguranță). */
    private ProviderException translate(SQLException e, String generic) {
        return switch (e.getErrorCode()) {
            case 2627, 2601 -> new ProviderException(
                    "În sala aleasă există deja o seanță la această oră. Alege altă sală sau altă oră.", e);
            case 547 -> new ProviderException(
                    "Baza de date a respins valorile: prețul trebuie să fie între 30 și 300, "
                            + "iar locurile libere între 0 și 400.", e);
            case 515 -> new ProviderException(
                    "Un câmp obligatoriu este gol. Completează toate câmpurile necesare.", e);
            default -> new ProviderException(generic + " (" + e.getMessage() + ")", e);
        };
    }
}