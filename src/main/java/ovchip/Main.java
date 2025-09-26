package ovchip;

import ovchip.dao.*;
import ovchip.domain.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final String URL  = "jdbc:postgresql://localhost:5432/ovchip";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            // DAOs
            ProductDAO pdao = new ProductDAOPsql(con);
            OVChipkaartDAO kdao = new OVChipkaartDAOPsql(con, pdao);
            ReizigerDAO rdao = new ReizigerDAOPsql(con, null, kdao);

            //Setup: Reiziger + Kaart + 2 Producten (nog zonder links)
            Reiziger r = new Reiziger(8080, "S.", null, "Tester", Date.valueOf(LocalDate.of(2002,3,15)));
            rdao.save(r);

            OVChipkaart k1 = new OVChipkaart(77700011, Date.valueOf("2028-12-31"), 2, 30.00);
            k1.setReiziger(r);
            kdao.save(k1);

            Product p1 = new Product(501, "Altijd Vrij", "Reizen waar/wanneer je wil", new BigDecimal("299.95"));
            Product p2 = new Product(502, "Dal Voordeel", "40% korting daluren", new BigDecimal("5.10"));
            pdao.save(p1);
            pdao.save(p2);

            //Links leggen en persist via kaartkant (update() schrijft koppeltabel)
            k1.addProduct(p1);
            k1.addProduct(p2);
            kdao.update(k1);

            //Check: alle producten bij kaart
            printProducts("Na eerste link (p1+p2)", pdao.findByOVChipkaart(k1));

            //Update links: p2 eraf, p3 erbij
            Product p3 = new Product(503, "Weekend Vrij", "Vrij reizen in weekend", new BigDecimal("31.00"));
            pdao.save(p3);

            k1.removeProduct(p2);
            k1.addProduct(p3);
            kdao.update(k1);

            printProducts("Na update link (p1+p3)", pdao.findByOVChipkaart(k1));

            //Delete product p1 koppellinks weg, kaart houdt alleen p3
            pdao.delete(p1);
            printProducts("Na delete p1", pdao.findByOVChipkaart(k1));

            //Delete kaart koppellinks weg, daarna cleanup rest
            kdao.delete(k1);
            printProducts("Na delete kaart", pdao.findByOVChipkaart(k1));

            // Cleanup leftover
            pdao.delete(p2);
            pdao.delete(p3);
            rdao.delete(r);
        }
    }

    private static void printProducts(String title, List<Product> producten) {
        System.out.println("-- " + title + " (count=" + producten.size() + ") --");
        for (Product p : producten) System.out.println(p);
    }
}
