package com.github.chaudhryfaisal.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collection;
import java.util.Map;


/**
 * Representation of a Humio API payload.
 */
@Builder
@Data
public class EventPayload {
    @Singular
    private Map<String, Object> tags;
    @Singular
    private Collection<Event> events;
}