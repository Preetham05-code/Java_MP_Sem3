package com.library.interfaces;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.model.Book;

/** Contract for catalog-management operations available to the Admin role. */
public interface Manageable {

    void addBook(Book book) throws DuplicateBookException;

    void deleteBook(String isbn) throws BookNotFoundException;

    void updateBook(String isbn, String newTitle, String newAuthor, String newCategory, int newTotalCopies)
            throws BookNotFoundException;
}
