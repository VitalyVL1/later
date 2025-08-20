package ru.practicum.item.dto;

import lombok.Builder;

import java.util.Set;


public record ItemDto(
        Long id,
        String normalUrl,
        String resolvedUrl,
        String mimeType,
        String title,
        boolean hasImage,
        boolean hasVideo,
        boolean unread,
        String dateResolved,
        Set<String> tags
) {
    @Builder(toBuilder = true)
    public ItemDto {
    }
}
