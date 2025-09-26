package ovchip.dao;

import ovchip.domain.Adres;
import ovchip.domain.OVChipkaart;
import ovchip.domain.Reiziger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReizigerDAOPsql implements ReizigerDAO {
    private final Connection con;
    private final AdresDAO adresDAO;
    private final OVChipkaartDAO ovChipkaartDAO;

    public ReizigerDAOPsql(Connection connection, AdresDAO adresDAO, OVChipkaartDAO ovChipkaartDAO) {
        this.con = connection;
        this.adresDAO = adresDAO;
        this.ovChipkaartDAO = ovChipkaartDAO;
    }

    @Override
    public boolean save(Reiziger r) {
        String sql = "INSERT INTO reiziger (reiziger_id, voorletters, tussenvoegsel, achternaam, geboortedatum) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getId());
            ps.setString(2, r.getVoorletters());
            ps.setString(3, r.getTussenvoegsel());
            ps.setString(4, r.getAchternaam());
            ps.setDate(5, new java.sql.Date(r.getGeboortedatum().getTime()));
            boolean ok = ps.executeUpdate() == 1;

            // adres via AdresDAO (blijft z'n eigen verantwoordelijkheid)
            if (ok && r.getAdres() != null) {
                if (adresDAO.findByReiziger(r) == null) adresDAO.save(r.getAdres());
                else adresDAO.update(r.getAdres());
            }
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(Reiziger r) {
        String sql = "UPDATE reiziger SET voorletters=?, tussenvoegsel=?, achternaam=?, geboortedatum=? WHERE reiziger_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getVoorletters());
            ps.setString(2, r.getTussenvoegsel());
            ps.setString(3, r.getAchternaam());
            ps.setDate(4, new java.sql.Date(r.getGeboortedatum().getTime()));
            ps.setInt(5, r.getId());
            boolean ok = ps.executeUpdate() == 1;

            if (ok && r.getAdres() != null) {
                if (adresDAO.findByReiziger(r) == null) adresDAO.save(r.getAdres());
                else adresDAO.update(r.getAdres());
            }
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Reiziger r) {
        try {
            for (OVChipkaart k : ovChipkaartDAO.findByReiziger(r)) {
                ovChipkaartDAO.delete(k);
            }
            Adres a = adresDAO.findByReiziger(r);
            if (a != null) adresDAO.delete(a);

            String sql = "DELETE FROM reiziger WHERE reiziger_id=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, r.getId());
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Reiziger findById(int id) {
        String sql = "SELECT * FROM reiziger WHERE reiziger_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reiziger r = mapRow(rs);
                    r.setAdres(adresDAO.findByReiziger(r));
                    for (OVChipkaart k : ovChipkaartDAO.findByReiziger(r)) r.addOVChipkaart(k);
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Reiziger> findByGbdatum(Date geboortedatum) {
        List<Reiziger> list = new ArrayList<>();
        String sql = "SELECT * FROM reiziger WHERE geboortedatum=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(geboortedatum.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reiziger r = mapRow(rs);
                    r.setAdres(adresDAO.findByReiziger(r));
                    for (OVChipkaart k : ovChipkaartDAO.findByReiziger(r)) r.addOVChipkaart(k);
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Reiziger> findAll() {
        List<Reiziger> list = new ArrayList<>();
        String sql = "SELECT * FROM reiziger ORDER BY reiziger_id";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reiziger r = mapRow(rs);
                r.setAdres(adresDAO.findByReiziger(r));
                for (OVChipkaart k : ovChipkaartDAO.findByReiziger(r)) r.addOVChipkaart(k);
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Reiziger mapRow(ResultSet rs) throws SQLException {
        return new Reiziger(
                rs.getInt("reiziger_id"),
                rs.getString("voorletters"),
                rs.getString("tussenvoegsel"),
                rs.getString("achternaam"),
                rs.getDate("geboortedatum")
        );
    }
}
