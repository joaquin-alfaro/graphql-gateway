package org.formentor.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookDto {
    private String id;
    private String name;
    private int pageCount;
    private AuthorDto author;
}
