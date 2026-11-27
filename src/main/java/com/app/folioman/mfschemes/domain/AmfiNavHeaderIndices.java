package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.util.SchemeConstants;
import org.springframework.util.StringUtils;

/**
 * Holds the column indices for parsing AMFI NAV data headers.
 * Dynamically determines the position of each field based on the header line.
 */
class AmfiNavHeaderIndices {
    int schemeCodeIdx = 0;
    int schemeNameIdx = 1;
    int isinIdx = 4;
    int isin2Idx = -1;
    int navIdx = 6;
    int dateIdx = 7;

    /**
     * Constructs the header indices by parsing the AMFI NAV header line.
     * Identifies the column positions for scheme code, name, ISIN, NAV value, and date.
     *
     * @param headerLine The header line from AMFI NAV data
     */
    AmfiNavHeaderIndices(String headerLine) {
        if (StringUtils.hasText(headerLine)) {
            String[] headers = headerLine.split(SchemeConstants.NAV_SEPARATOR);
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim();
                if (header.equalsIgnoreCase("Scheme Code")) schemeCodeIdx = i;
                else if (header.equalsIgnoreCase("Scheme Name") || header.equalsIgnoreCase("NAV Name"))
                    schemeNameIdx = i;
                else if (header.contains("ISIN Div Payout") && !header.contains("Reinvestment")) isinIdx = i;
                else if (header.contains("ISIN Div Reinvestment")) isin2Idx = i;
                else if (header.equalsIgnoreCase("Net Asset Value")) navIdx = i;
                else if (header.equalsIgnoreCase("Date")) dateIdx = i;
            }
        }
    }
}
