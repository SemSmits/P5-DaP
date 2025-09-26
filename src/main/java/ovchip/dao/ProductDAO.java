package ovchip.dao;

import ovchip.domain.OVChipkaart;
import ovchip.domain.Product;
import java.sql.SQLException;
import java.util.List;

public interface ProductDAO {
    boolean save(Product p) throws SQLException;
    boolean update(Product p) throws SQLException;
    boolean delete(Product p) throws SQLException;
    List<Product> findByOVChipkaart(OVChipkaart k) throws SQLException;
    List<Product> findAll() throws SQLException;
}


