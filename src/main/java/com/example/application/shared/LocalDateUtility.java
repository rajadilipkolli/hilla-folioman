package com.example.application.shared;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateUtility {

    public static LocalDate parse(String from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        return LocalDate.parse(from, formatter);
    }
}
