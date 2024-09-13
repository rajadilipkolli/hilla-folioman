/* Licensed under Apache-2.0 2022. */
package com.example.application.portfolio.models;

import java.io.Serializable;

public record InvestorInfoDTO(String email, String name, String mobile, String address) implements Serializable {}
