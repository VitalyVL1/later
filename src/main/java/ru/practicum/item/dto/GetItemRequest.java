package ru.practicum.item.dto;

import lombok.Builder;

import java.util.Set;

public record GetItemRequest(
        long userId,
        State state,
        ContentType contentType,
        Sort sort,
        int limit,
        Set<String> tags
) {
    @Builder(toBuilder = true)
    public GetItemRequest {
    }

    public enum State {UNREAD, READ, ALL}

    public enum ContentType {ARTICLE, VIDEO, IMAGE, ALL}

    public enum Sort {NEWEST, OLDEST, TITLE, SITE}
}
