package com.github.chaudhryfaisal.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

/**
 * Representation of a Humio single event.
 */
@Builder
@Data
public class Event {
    long timestamp;
    @Singular
    private Map<String, Object> attributes;
}
