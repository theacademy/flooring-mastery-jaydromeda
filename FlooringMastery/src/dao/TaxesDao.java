package dao;

import dto.Taxes;

public interface TaxesDao {

    Taxes getTaxesInfo(String tax)
        throws Exception;
}
