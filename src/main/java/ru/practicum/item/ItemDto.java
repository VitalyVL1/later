package ru.practicum.item;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private Long userId;
    private String url;
    private Set<String> tags;
}
