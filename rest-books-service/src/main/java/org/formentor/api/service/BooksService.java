package org.formentor.api.service;

import org.formentor.api.dto.AuthorDto;
import org.formentor.api.dto.BookDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BooksService {
    private final static AuthorDto author = AuthorDto.builder().id(1).firstName("Benito").lastName("Pérez Galdós").build();
    private final static List<BookDto> books = new ArrayList<>();
    static {
        books.add(BookDto.builder().id("one").name("Trafalgar").author(author).build());
        books.add(BookDto.builder().id("two").name("La España de Fernando VII").author(author).build());
        books.add(BookDto.builder().id("three").name("Cristinos y carlistas").author(author).build());
    }

    public List<BookDto> listBooks() {
        return books;
    }

    public Optional<BookDto> findById(String id) {
        return books.stream().filter(bookDto -> bookDto.getId().equals(id)).findFirst();
    }
}
