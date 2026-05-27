package com.recon.service.infrastructure.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.domain.model.ChangeEventType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CanalMessageParser {

    private final ObjectMapper objectMapper;

    public CanalMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public ChangeEvent parse(String rawMessage) {
        try {
            Map<String, Object> map = objectMapper.readValue(rawMessage, new TypeReference<Map<String, Object>>() {
            });
            ChangeEvent event = new ChangeEvent();
            event.setDestination(requiredString(map, "destination"));
            event.setDatabaseName(requiredString(map, "databaseName"));
            event.setTableName(requiredString(map, "tableName"));
            event.setEventType(ChangeEventType.valueOf(requiredString(map, "eventType")));
            event.setPrimaryKey(requiredString(map, "primaryKey"));
            event.setBinlogFile(stringValue(map.get("binlogFile")));
            event.setBinlogPosition(requiredString(map, "binlogPosition"));
            event.setBefore((Map<String, Object>) map.get("before"));
            event.setAfter((Map<String, Object>) map.get("after"));
            event.setChangedFields(toStringSet(map.get("changedFields")));
            event.setRawMessage(rawMessage);
            event.setReceiveTime(Instant.now());
            event.setEventKey(ChangeEvent.buildEventKey(
                    event.getDestination(),
                    event.getDatabaseName(),
                    event.getTableName(),
                    event.getPrimaryKey(),
                    event.getEventType(),
                    event.getBinlogPosition()));
            return event;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse Canal message", ex);
        }
    }

    private String requiredString(Map<String, Object> map, String key) {
        String value = stringValue(map.get(key));
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Set<String> toStringSet(Object value) {
        Set<String> result = new LinkedHashSet<String>();
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }
}
