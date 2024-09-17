/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.models;

import java.io.Serializable;

public record ValuationDTO(String date, double nav, double value) implements Serializable {}
