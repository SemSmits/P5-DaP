package ovchip;

import ovchip.dao.*;
import ovchip.domain.Reiziger;
import ovchip.domain.OVChipkaart;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class Main {
    private static final String URL  = "jdbc:postgresql://localhost:5432/ovchip";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            OVChipkaartDAO ovdao   = new OVChipkaartDAOPsql(con);
            AdresDAO adresdao = new AdresDAOPsql(con);
            ReizigerDAO rdao       = new ReizigerDAOPsql(con, adresdao, ovdao);

            // 1) Testdata aanmaken
            Reiziger sem = new Reiziger(
                    777,
                    "S.",
                    null,
                    "Smits",
                    Date.valueOf(LocalDate.of(2002, 3, 15))
            );

            OVChipkaart k1 = new OVChipkaart(
                    90007771,
                    Date.valueOf(LocalDate.of(2027, 1, 1)),
                    2,
                    25.00
            );
            OVChipkaart k2 = new OVChipkaart(
                    90007772,
                    Date.valueOf(LocalDate.of(2026, 6, 30)),
                    1,
                    7.50
            );
            sem.addOVChipkaart(k1);
            sem.addOVChipkaart(k2);

            // 2) SAVE: Reiziger + kaarten
            System.out.println("== SAVE ==");
            boolean saved = rdao.save(sem);
            System.out.println("Reiziger save: " + saved);
            printKaarten("Na save – alle kaarten", ovdao.findAll());
            System.out.println(sem);

            // 3) UPDATE: wijzig naam + kaarten (verwijder k2, voeg k3 toe, update k1 saldo)
            System.out.println("\n== UPDATE ==");
            sem.setAchternaam("Smits-Updated");

            sem.removeOVChipkaart(k2);
            OVChipkaart k3 = new OVChipkaart(
                    90007773,
                    Date.valueOf(LocalDate.of(2028, 12, 31)),
                    2,
                    40.00
            );
            sem.addOVChipkaart(k3);

            k1.setSaldo(31.25);

            boolean updated = rdao.update(sem);
            System.out.println("Reiziger update: " + updated);
            printKaarten("Na update – alle kaarten", ovdao.findAll());
            System.out.println(sem);

            // 4) DELETE: verwijder reiziger + cascade via DAO (eerst kaarten, dan reiziger)
            System.out.println("\n== DELETE ==");
            boolean deleted = rdao.delete(sem);
            System.out.println("Reiziger delete: " + deleted);
            printKaarten("Na delete – alle kaarten", ovdao.findAll());
        }
    }

    private static void printKaarten(String title, List<OVChipkaart> kaarten) {
        System.out.println("-- " + title + " (count=" + kaarten.size() + ") --");
        for (OVChipkaart k : kaarten) {
            System.out.println(k);
        }
    }
}
