package ovchip.dao;

import ovchip.domain.OVChipkaart;
import ovchip.domain.Reiziger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OVChipkaartDAOPsql implements OVChipkaartDAO {
    private final Connection con;

    public OVChipkaartDAOPsql(Connection connection) {
        this.con = connection;
    }

    @Override
    public boolean save(OVChipkaart k) throws SQLException {
        String sql = "INSERT INTO ov_chipkaart (kaart_nummer, geldig_tot, klasse, saldo, reiziger_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, k.getKaartNummer());
            ps.setDate(2, new java.sql.Date(k.getGeldigTot().getTime()));
            ps.setInt(3, k.getKlasse());
            ps.setDouble(4, k.getSaldo());
            if (k.getReiziger() != null) ps.setInt(5, k.getReiziger().getId());
            else ps.setNull(5, Types.INTEGER);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean update(OVChipkaart k) {
        String sql = "UPDATE ov_chipkaart SET geldig_tot=?, klasse=?, saldo=?, reiziger_id=? WHERE kaart_nummer=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(k.getGeldigTot().getTime()));
            ps.setInt(2, k.getKlasse());
            ps.setDouble(3, k.getSaldo());
            if (k.getReiziger() != null) ps.setInt(4, k.getReiziger().getId());
            else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, k.getKaartNummer());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        }
    }

    @Override
    public boolean delete(OVChipkaart k) {
        String sql = "DELETE FROM ov_chipkaart WHERE kaart_nummer=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, k.getKaartNummer());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        }
    }

    @Override
    public List<OVChipkaart> findByReiziger(Reiziger r) {
        List<OVChipkaart> list = new ArrayList<>();
        String sql = "SELECT kaart_nummer, geldig_tot, klasse, saldo FROM ov_chipkaart WHERE reiziger_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OVChipkaart k = new OVChipkaart(
                            rs.getInt("kaart_nummer"),
                            rs.getDate("geldig_tot"),
                            rs.getInt("klasse"),
                            rs.getDouble("saldo")
                    );
                    k.setReiziger(r);
                    list.add(k);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
