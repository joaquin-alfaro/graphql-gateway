package org.formentor.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorDto {
    private int id;
    private String firstName;
    private String lastName;
    /**
     * TODO
     * Adds an attribute of type LocalDate
     */
//    private LocalDate birthDate;
}
