package org.formentor.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CountryDto {
    private String iso;
    private String name;
    private List<CityDto> cities;
}
