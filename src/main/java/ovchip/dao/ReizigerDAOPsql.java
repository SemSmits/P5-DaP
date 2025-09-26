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
    private final OVChipkaartDAO ovdao;

    public ReizigerDAOPsql(Connection connection, AdresDAO adresDAO, OVChipkaartDAO ovdao) {
        this.con = connection;
        this.adresDAO = adresDAO;
        this.ovdao = ovdao;
    }

    @Override
    public boolean save(Reiziger inReiziger) throws SQLException {
        PreparedStatement statement = this.con.prepareStatement(
                "INSERT INTO reiziger (reiziger_id, voorletters, tussenvoegsel, achternaam, geboortedatum) VALUES (?, ?, ?, ?, ?)"
        );
        statement.setInt(1, inReiziger.getId());
        statement.setString(2, inReiziger.getVoorletters());
        statement.setString(3, inReiziger.getTussenvoegsel());
        statement.setString(4, inReiziger.getAchternaam());
        statement.setDate(5, new java.sql.Date(inReiziger.getGeboortedatum().getTime()));
        statement.execute();
        statement.close();

        if (this.ovdao != null && inReiziger.getKaarten() != null) {
            for (OVChipkaart kaart : inReiziger.getKaarten()) {
                kaart.setReiziger(inReiziger);
                this.ovdao.save(kaart);
            }
        }
        return true;
    }

    @Override
    public boolean update(Reiziger inReiziger) throws SQLException {
        PreparedStatement statement = this.con.prepareStatement(
                "UPDATE reiziger SET voorletters = ?, tussenvoegsel = ?, achternaam = ?, geboortedatum = ? WHERE reiziger_id = ?"
        );
        statement.setString(1, inReiziger.getVoorletters());
        statement.setString(2, inReiziger.getTussenvoegsel());
        statement.setString(3, inReiziger.getAchternaam());
        statement.setDate(4, new java.sql.Date(inReiziger.getGeboortedatum().getTime()));
        statement.setInt(5, inReiziger.getId());
        statement.execute();
        statement.close();

        if (this.ovdao != null) {
            for (OVChipkaart bestaand : this.ovdao.findByReiziger(inReiziger)) {
                this.ovdao.delete(bestaand);
            }
            if (inReiziger.getKaarten() != null) {
                for (OVChipkaart kaart : inReiziger.getKaarten()) {
                    kaart.setReiziger(inReiziger);
                    this.ovdao.save(kaart);
                }
            }
        }
        return true;
    }

    @Override
    public boolean delete(Reiziger inReiziger) throws SQLException {
        if (this.ovdao != null) {
            for (OVChipkaart kaart : this.ovdao.findByReiziger(inReiziger)) {
                this.ovdao.delete(kaart);
            }
        }

        PreparedStatement statement = this.con.prepareStatement(
                "DELETE FROM reiziger WHERE reiziger_id = ?"
        );
        statement.setInt(1, inReiziger.getId());
        statement.execute();
        statement.close();

        return true;
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
                    for (OVChipkaart k : ovdao.findByReiziger(r)) r.addOVChipkaart(k);
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
                    for (OVChipkaart k : ovdao.findByReiziger(r)) r.addOVChipkaart(k);
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
                for (OVChipkaart k : ovdao.findByReiziger(r)) r.addOVChipkaart(k);
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
