package ru.practicum.item.dto;

import java.util.Set;

public record AddItemRequest(
        String url,
        Set<String> tags
) {
}
