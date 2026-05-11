package dao;

import dto.Taxes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaxesDaoStubImpl implements TaxesDao {

    private final Map<String, Taxes> taxes = new LinkedHashMap<>();

    public TaxesDaoStubImpl() {
        taxes.put("TX", makeTaxes("TX", "Texas",      "4.45"));
        taxes.put("CA", makeTaxes("CA", "California", "25.00"));
        taxes.put("WA", makeTaxes("WA", "Washington", "9.25"));
    }

    @Override
    public Taxes getTaxesInfo(String stateAbbreviation) {
        return taxes.get(stateAbbreviation.toUpperCase().trim());
    }

    private Taxes makeTaxes(String abbr, String name, String rate) {
        Taxes t = new Taxes();
        t.setState(abbr);
        t.setStateName(name);
        t.setTaxRate(new BigDecimal(rate));
        return t;
    }
}
