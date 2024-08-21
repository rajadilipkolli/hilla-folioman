package com.example.application.mfschemes.models.response;

import java.io.Serializable;
import java.util.List;

public record NavResponse(String status, MetaDTO meta, List<SchemeNAVDataDTO> data) implements Serializable {}
