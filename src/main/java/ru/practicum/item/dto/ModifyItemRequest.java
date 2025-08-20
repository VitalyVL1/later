package ru.practicum.item.dto;

import lombok.Builder;

import java.util.Set;

public record ModifyItemRequest(
        long itemId,
        boolean read,
        Set<String> tags,
        boolean replaceTags

) {
    @Builder(toBuilder = true)
    public ModifyItemRequest {
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }
}
