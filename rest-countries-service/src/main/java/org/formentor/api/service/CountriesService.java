package org.formentor.api.service;

import org.formentor.api.dto.CityDto;
import org.formentor.api.dto.CountryDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CountriesService {
    private final static List<CityDto> spain = new ArrayList<>();
    private final static List<CityDto> germany = new ArrayList<>();
    private final static List<CountryDto> countries = new ArrayList<>();
    static {
        spain.add(CityDto.builder().id(1).name("Palma").build());
        spain.add(CityDto.builder().id(2).name("Albacete").build());
        germany.add(CityDto.builder().id(3).name("Berlin").build());
        germany.add(CityDto.builder().id(4).name(" Wuppertal").build());
        countries.add(CountryDto.builder().iso("ES").name("Spain").cities(spain).build());
        countries.add(CountryDto.builder().iso("DE").name("Germany").cities(germany).build());
    }

    public List<CountryDto> listCountries() {
        return countries;
    }

    public Optional<CountryDto> findByIso(String iso) {
        return countries.stream().filter(countryDto -> countryDto.getIso().equals(iso)).findFirst();
    }
}
