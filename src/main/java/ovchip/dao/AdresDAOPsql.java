package ovchip.dao;

import ovchip.domain.Adres;
import ovchip.domain.Reiziger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdresDAOPsql implements AdresDAO {

    private Connection con;

    public AdresDAOPsql(Connection connection) {
        this.con = connection;
    }

    @Override
    public boolean save(Adres adres) {
        try {
            String query = "INSERT INTO Adres (adres_id, postcode, huisnummer, straat, woonplaats, reiziger_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, adres.getId());
            preparedStatement.setString(2, adres.getPostcode());
            preparedStatement.setString(3, adres.getHuisnummer());
            preparedStatement.setString(4, adres.getStraat());
            preparedStatement.setString(5, adres.getWoonplaats());
            preparedStatement.setInt(6, adres.getReiziger().getId());
            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Adres adres) {
        try {
            String query = "UPDATE Adres SET postcode = ?, huisnummer = ?, straat = ?, woonplaats = ?, reiziger_id = ? WHERE adres_id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, adres.getPostcode());
            preparedStatement.setString(2, adres.getHuisnummer());
            preparedStatement.setString(3, adres.getStraat());
            preparedStatement.setString(4, adres.getWoonplaats());
            preparedStatement.setInt(5, adres.getReiziger().getId());
            preparedStatement.setInt(6, adres.getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(Adres adres) {
        try {
            String query = "DELETE FROM Adres WHERE adres_id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, adres.getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Adres findByReiziger(Reiziger reiziger) {
        try {
            String query = "SELECT * FROM Adres WHERE reiziger_id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, reiziger.getId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Adres(
                        resultSet.getInt("adres_id"),
                        resultSet.getString("postcode"),
                        resultSet.getString("huisnummer"),
                        resultSet.getString("straat"),
                        resultSet.getString("woonplaats")
                );
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    @Override
    public List<Adres> findAll() {
        List<Adres> result = new ArrayList<>();
        try {
            ReizigerDAO rdao = new ReizigerDAOPsql(con, this, null);
            List<Reiziger> reizigers = rdao.findAll();

            for (Reiziger r : reizigers) {
                Adres a = r.getAdres();
                if (a != null) {
                    a.setReiziger(r);
                    result.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Adres findById(int id) {
        String sql = "SELECT adres_id, postcode, huisnummer, straat, woonplaats, reiziger_id " +
                "FROM adres WHERE adres_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Adres a = new Adres(
                        rs.getInt("adres_id"),
                        rs.getString("postcode"),
                        rs.getString("huisnummer"),
                        rs.getString("straat"),
                        rs.getString("woonplaats")
                );

                int reizigerId = rs.getInt("reiziger_id");
                if (reizigerId > 0) {
                    ReizigerDAO rdao = new ReizigerDAOPsql(con, this, null);
                    Reiziger r = rdao.findById(reizigerId);
                    if (r != null) {
                        a.setReiziger(r);
                        r.setAdres(a);
                    }
                }
                return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


}
