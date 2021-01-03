package org.formentor.api.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.formentor.api.dto.BookDto;
import org.formentor.api.service.BooksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/books",
        produces = MediaType.APPLICATION_JSON_VALUE,
        headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE
)
public class BooksController {

    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @ApiOperation(
            tags = "API Books",
            value = "Returns a list of all books")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/")
    public ResponseEntity<List<BookDto>> books() {
        return new ResponseEntity<>(booksService.listBooks(), HttpStatus.OK);
    }

    @ApiOperation(
            tags = "API Books",
            value = "Returns book by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> bookById(@PathVariable("id") String id) {
        Optional<BookDto> bookDto = booksService.findById(id);
        if (bookDto.isPresent()) {
            return new ResponseEntity<>(bookDto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(
            tags = "API Books",
            value = "Returns book in library by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/library/{id}")
    public ResponseEntity<List<BookDto>> booksByLibraryId(@PathVariable("id") String id) {
        return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
    }
}
