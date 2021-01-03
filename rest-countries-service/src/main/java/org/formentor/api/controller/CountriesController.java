package org.formentor.api.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.formentor.api.dto.CountryDto;
import org.formentor.api.service.CountriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/countries",
        produces = MediaType.APPLICATION_JSON_VALUE,
        headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE
)
public class CountriesController {

    private final CountriesService countriesService;

    public CountriesController(CountriesService countriesService) {
        this.countriesService = countriesService;
    }

    @ApiOperation(
            tags = "API Countries",
            value = "Returns a list of all countries")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/")
    public ResponseEntity<List<CountryDto>> countries() {
        return new ResponseEntity<>(countriesService.listCountries(), HttpStatus.OK);
    }

    @ApiOperation(
            tags = "API Countries",
            value = "Returns country by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CountryDto> countryById(@PathVariable("id") String id) {
        Optional<CountryDto> countryDto = countriesService.findByIso(id);
        if (countryDto.isPresent()) {
            return new ResponseEntity<>(countryDto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
