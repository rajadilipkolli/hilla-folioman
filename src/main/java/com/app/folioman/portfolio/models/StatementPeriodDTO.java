/* Licensed under Apache-2.0 2022. */
package com.app.folioman.portfolio.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record StatementPeriodDTO(String from, @JsonProperty("to") String myto) implements Serializable {}
