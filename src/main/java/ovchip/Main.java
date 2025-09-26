package ovchip;

import ovchip.dao.*;
import ovchip.domain.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/ovchip", "postgres", "postgres");

            AdresDAO adresDAO = new AdresDAOPsql(conn);
            OVChipkaartDAO ovDAO = new OVChipkaartDAOPsql(conn);
            ReizigerDAO reizigerDAO = new ReizigerDAOPsql(conn, adresDAO, ovDAO);

            Reiziger r = new Reiziger(900, "S", null, "Student", new Date());
            Adres a = new Adres(900, "3511AB", "10", "Nijverheidsweg", "Utrecht");
            a.setReiziger(r);
            r.setAdres(a);

            OVChipkaart kaart = new OVChipkaart(9900, new Date(), 2, 25.0);
            r.addOVChipkaart(kaart);

            reizigerDAO.save(r);
            adresDAO.save(a);
            ovDAO.save(kaart);

            List<Reiziger> all = reizigerDAO.findAll();
            all.forEach(System.out::println);

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
