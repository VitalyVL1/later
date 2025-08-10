package ru.practicum.note;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemNoteDto {
    private Long id;
    private Long itemId;
    private String text;
    private String dateOfNote;
    private String itemUrl;
}
