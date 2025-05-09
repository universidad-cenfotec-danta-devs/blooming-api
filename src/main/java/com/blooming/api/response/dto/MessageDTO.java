package com.blooming.api.response.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {

    @JsonProperty("content")
    private String content;

    @JsonProperty("type")
    private String type;
}