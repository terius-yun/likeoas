package com.hanya.hetos.book.web;

import com.hanya.hetos.aop.AddHateoasLinks;
import com.hanya.hetos.book.domain.*;
import com.hanya.hetos.book.web.dto.BookListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RequestMapping("/books")
@RestController
public class BookController {
    private final BookRepository repository;

    @AddHateoasLinks(controller = BookController.class, method = "getAllBooks")
    @GetMapping
    public ResponseEntity<BookListVO> getAllBooks() {
        List<Book> bookEntities = repository.findAll();
        BookListVO books = BookListVO.builder().books(bookEntities).build();;

        return ResponseEntity.ok(books);
    }

    @AddHateoasLinks(controller = BookController.class, method = "getBook")
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        return ResponseEntity.ok(book);
    }
}
