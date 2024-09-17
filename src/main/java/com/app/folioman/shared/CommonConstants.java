package com.app.folioman.shared;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CommonConstants {

    private static final String DATE_PATTERN_DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final DateTimeFormatter FORMATTER_DD_MMM_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MMM_YYYY, Locale.ENGLISH);
}
