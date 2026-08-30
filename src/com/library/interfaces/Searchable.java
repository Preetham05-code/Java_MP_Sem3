package com.library.interfaces;

import com.library.model.Book;
import java.util.List;

/** Contract for search operations available to the Member role. */
public interface Searchable {

    List<Book> searchByTitle(String title);

    List<Book> searchByAuthor(String author);

    List<Book> searchByCategory(String category);
}
