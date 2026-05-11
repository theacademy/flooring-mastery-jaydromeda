package dao;

import dto.Taxes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaxesDaoImpl implements TaxesDao {

    private static final String TAXES_FILE = "Data/Taxes.txt";
    private static final String DELIMITER = ",";

    private Map<String, Taxes> taxesCache = null;

    @Override
    public Taxes getTaxesInfo(String stateAbbreviation) throws Exception {
        Map<String, Taxes> cache = getCache();
        return cache.get(stateAbbreviation.toUpperCase().trim());
        // Returns null if not found
    }

     // Returns the cache
    private Map<String, Taxes> getCache() throws Exception {
        if (taxesCache == null) {
            taxesCache = loadTaxesFromFile();
        }
        return taxesCache;
    }

    /**
      Reads Data/Taxes.txt and returns all entries keyed by uppercase state abbreviation.
      Expected column order:
        0  StateAbbreviation
        1  StateName
        2  TaxRate
     */
    private Map<String, Taxes> loadTaxesFromFile() throws Exception {
        Map<String, Taxes> map = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(TAXES_FILE))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("State")) continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                Taxes taxes = unmarshallTaxes(line);
                map.put(taxes.getState().toUpperCase(), taxes);
            }
        } catch (IOException e) {
            throw new Exception("Error reading taxes file: " + e.getMessage(), e);
        }

        return map;
    }

    private Taxes unmarshallTaxes(String line) throws Exception {
        String[] tokens = line.split(DELIMITER);

        if (tokens.length != 3) {
            throw new Exception("Malformed taxes line (expected 3 fields, got "
                    + tokens.length + "): " + line);
        }

        try {
            Taxes taxes = new Taxes();
            taxes.setState(tokens[0].trim().toUpperCase());
            taxes.setStateName(tokens[1].trim());
            taxes.setTaxRate(new BigDecimal(tokens[2].trim()));
            return taxes;
        } catch (NumberFormatException e) {
            throw new Exception("Could not parse tax rate in taxes line: " + line, e);
        }
    }
}