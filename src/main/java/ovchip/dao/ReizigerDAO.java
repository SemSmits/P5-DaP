package ovchip.dao;

import ovchip.domain.Reiziger;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;

public interface ReizigerDAO {
    boolean save(Reiziger reiziger) throws SQLException;
    boolean update(Reiziger reiziger);
    boolean delete(Reiziger reiziger);
    Reiziger findById(int id);
    List<Reiziger> findByGbdatum(Date geboortedatum);
    List<Reiziger> findAll();
}

