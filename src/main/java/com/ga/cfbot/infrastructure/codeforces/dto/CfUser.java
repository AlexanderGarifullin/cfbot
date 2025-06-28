package com.ga.cfbot.infrastructure.codeforces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfUser {
    public String handle;
    public Integer rating;
}
