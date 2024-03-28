package com.hanya.hetos.book.web.dto;

import com.hanya.hetos.book.domain.Book;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookListVO {
    List<Book> books;
}
