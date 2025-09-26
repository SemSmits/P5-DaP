package ovchip.dao;

import ovchip.domain.OVChipkaart;
import ovchip.domain.Product;
import ovchip.domain.Reiziger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OVChipkaartDAOPsql implements OVChipkaartDAO {
    private final Connection con;
    private final ProductDAO pdao;

    public OVChipkaartDAOPsql(Connection connection, ProductDAO pdao) {
        this.con = connection;
        this.pdao = pdao;
    }

    @Override
    public boolean save(OVChipkaart k) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ov_chipkaart (kaart_nummer, geldig_tot, klasse, saldo, reiziger_id) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, k.getKaartNummer());
            ps.setDate(2, new java.sql.Date(k.getGeldigTot().getTime()));
            ps.setInt(3, k.getKlasse());
            ps.setDouble(4, k.getSaldo());
            ps.setInt(5, k.getReiziger().getId());
            if (ps.executeUpdate() == 0) return false;
        }
        insertLinks(k);
        return true;
    }

    @Override
    public boolean update(OVChipkaart k) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE ov_chipkaart SET geldig_tot = ?, klasse = ?, saldo = ?, reiziger_id = ? WHERE kaart_nummer = ?")) {
            ps.setDate(1, new java.sql.Date(k.getGeldigTot().getTime()));
            ps.setInt(2, k.getKlasse());
            ps.setDouble(3, k.getSaldo());
            ps.setInt(4, k.getReiziger().getId());
            ps.setInt(5, k.getKaartNummer());
            if (ps.executeUpdate() == 0) return false;
        }
        deleteLinks(k.getKaartNummer());
        insertLinks(k);
        return true;
    }

    @Override
    public boolean delete(OVChipkaart k) throws SQLException {
        deleteLinks(k.getKaartNummer());
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM ov_chipkaart WHERE kaart_nummer = ?")) {
            ps.setInt(1, k.getKaartNummer());
            return ps.executeUpdate() > 0;
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

    @Override
    public List<OVChipkaart> findAll() throws SQLException {
        List<OVChipkaart> list = new ArrayList<>();
        final String sql = "SELECT kaart_nummer, geldig_tot, klasse, saldo, reiziger_id FROM ov_chipkaart ORDER BY kaart_nummer";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OVChipkaart k = new OVChipkaart(
                        rs.getInt("kaart_nummer"),
                        rs.getDate("geldig_tot"),
                        rs.getInt("klasse"),
                        rs.getDouble("saldo")
                );

                k.getProducts().clear();
                k.getProducts().addAll(pdao.findByOVChipkaart(k));

                list.add(k);
            }
        }
        return list;
    }

    private void deleteLinks(int kaartNummer) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM ov_chipkaart_product WHERE kaart_nummer = ?")) {
            ps.setInt(1, kaartNummer);
            ps.executeUpdate();
        }
    }

    private void insertLinks(OVChipkaart k) throws SQLException {
        if (k.getProducts() == null || k.getProducts().isEmpty()) return;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ov_chipkaart_product (kaart_nummer, product_nummer) VALUES (?, ?)")) {
            for (Product p : k.getProducts()) {
                ps.setInt(1, k.getKaartNummer());
                ps.setInt(2, p.getProductNummer());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }


}
