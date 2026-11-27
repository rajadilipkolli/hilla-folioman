package com.app.folioman.shared;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CommonConstants {

    private static final String DATE_PATTERN_DD_MMM_YYYY = "dd-MMM-yyyy";
    private static final String DATE_PATTERN_MMM_D_YYYY = "MMM d yyyy";
    public static final DateTimeFormatter FORMATTER_DD_MMM_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MMM_YYYY, Locale.ENGLISH);
    public static final DateTimeFormatter FORMATTER_MMM_D_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_MMM_D_YYYY, Locale.ENGLISH);
}
