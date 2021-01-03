package org.formentor.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityDto {
    private int id;
    private String name;
}
