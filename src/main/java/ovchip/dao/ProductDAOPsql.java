package ovchip.dao;

import ovchip.domain.OVChipkaart;
import ovchip.domain.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOPsql implements ProductDAO {
    private final Connection con;
    public ProductDAOPsql(Connection con) { this.con = con; }

    @Override
    public boolean save(Product p) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO product (product_nummer, naam, beschrijving, prijs) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, p.getProductNummer());
            ps.setString(2, p.getNaam());
            ps.setString(3, p.getBeschrijving());
            ps.setBigDecimal(4, p.getPrijs());
            if (ps.executeUpdate() == 0) return false;
        }

        if (p.getKaarten() != null) {
            try (PreparedStatement link = con.prepareStatement(
                    "INSERT INTO ov_chipkaart_product (kaart_nummer, product_nummer) VALUES (?, ?)")) {
                for (OVChipkaart k : p.getKaarten()) {
                    link.setInt(1, k.getKaartNummer());
                    link.setInt(2, p.getProductNummer());
                    link.addBatch();
                }
                link.executeBatch();
            }
        }
        return true;
    }

    @Override
    public boolean update(Product p) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE product SET naam = ?, beschrijving = ?, prijs = ? WHERE product_nummer = ?")) {
            ps.setString(1, p.getNaam());
            ps.setString(2, p.getBeschrijving());
            ps.setBigDecimal(3, p.getPrijs());
            ps.setInt(4, p.getProductNummer());
            if (ps.executeUpdate() == 0) return false;
        }

        try (PreparedStatement del = con.prepareStatement(
                "DELETE FROM ov_chipkaart_product WHERE product_nummer = ?")) {
            del.setInt(1, p.getProductNummer());
            del.executeUpdate();
        }
        if (p.getKaarten() != null) {
            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO ov_chipkaart_product (kaart_nummer, product_nummer) VALUES (?, ?)")) {
                for (OVChipkaart k : p.getKaarten()) {
                    ins.setInt(1, k.getKaartNummer());
                    ins.setInt(2, p.getProductNummer());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
        return true;
    }

    @Override
    public boolean delete(Product p) throws SQLException {
        try (PreparedStatement delLinks = con.prepareStatement(
                "DELETE FROM ov_chipkaart_product WHERE product_nummer = ?")) {
            delLinks.setInt(1, p.getProductNummer());
            delLinks.executeUpdate();
        }
        try (PreparedStatement del = con.prepareStatement(
                "DELETE FROM product WHERE product_nummer = ?")) {
            del.setInt(1, p.getProductNummer());
            return del.executeUpdate() > 0;
        }
    }

    @Override
    public List<Product> findByOVChipkaart(OVChipkaart k) throws SQLException {
        List<Product> list = new ArrayList<>();
        final String sql = """
            SELECT p.product_nummer, p.naam, p.beschrijving, p.prijs
            FROM product p
            JOIN ov_chipkaart_product kp ON kp.product_nummer = p.product_nummer
            WHERE kp.kaart_nummer = ?
            ORDER BY p.product_nummer
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, k.getKaartNummer());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("product_nummer"),
                            rs.getString("naam"),
                            rs.getString("beschrijving"),
                            rs.getBigDecimal("prijs")
                    );

                    p.addKaart(k);
                    list.add(p);
                }
            }
        }
        return list;
    }

    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT product_nummer, naam, beschrijving, prijs FROM product ORDER BY product_nummer");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("product_nummer"),
                        rs.getString("naam"),
                        rs.getString("beschrijving"),
                        rs.getBigDecimal("prijs")
                ));
            }
        }
        return list;
    }
}
