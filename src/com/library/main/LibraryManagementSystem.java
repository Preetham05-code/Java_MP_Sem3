package com.library.main;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.exception.InvalidCredentialsException;
import com.library.exception.InvalidOperationException;
import com.library.exception.LibraryException;
import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.service.Library;

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point for the University Library Management System.
 * Provides separate menus for Admin (catalog management) and Member
 * (search / borrow / return) roles, as required by the micro-project.
 */
public class LibraryManagementSystem {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = Library.getInstance();

    public static void main(String[] args) {
        library.loadData();
        library.startBackgroundServices();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                library.saveData();
                library.shutdownBackgroundServices();
            }
        });

        System.out.println("=======================================================");
        System.out.println("     UNIVERSITY LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=======================================================");
        System.out.println("(Default admin login -> username: admin | password: admin123)");

        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = readInt();
            switch (choice) {
                case 1:
                    adminLogin();
                    break;
                case 2:
                    memberLogin();
                    break;
                case 3:
                    registerMember();
                    break;
                case 4:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        library.saveData();
        library.shutdownBackgroundServices();
        System.out.println("Thank you for using the Library Management System. Goodbye!");
        scanner.close();
    }

    // ---------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------

    private static void printMainMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Admin Login");
        System.out.println("2. Member Login");
        System.out.println("3. New Member Registration");
        System.out.println("4. Exit");
        System.out.print("Enter choice: ");
    }

    private static void adminLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            Admin admin = library.authenticateAdmin(username, password);
            System.out.println("Login successful. Welcome, " + admin.getName() + "!");
            adminMenu(admin);
        } catch (InvalidCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void adminMenu(Admin admin) {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n--- ADMIN MENU (" + admin.getName() + ") ---");
            System.out.println("1. Add Book");
            System.out.println("2. Delete Book");
            System.out.println("3. Update Book");
            System.out.println("4. View All Books");
            System.out.println("5. View All Members");
            System.out.println("6. View All Borrow Records");
            System.out.println("7. View Overdue Books");
            System.out.println("8. Logout");
            System.out.print("Enter choice: ");
            int choice = readInt();
            try {
                switch (choice) {
                    case 1:
                        addBookFlow();
                        break;
                    case 2:
                        deleteBookFlow();
                        break;
                    case 3:
                        updateBookFlow();
                        break;
                    case 4:
                        viewAllBooks();
                        break;
                    case 5:
                        viewAllMembers();
                        break;
                    case 6:
                        viewAllRecords();
                        break;
                    case 7:
                        viewOverdue();
                        break;
                    case 8:
                        logout = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void memberLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            Member member = library.authenticateMember(username, password);
            System.out.println("Login successful. Welcome, " + member.getName() + "!");
            memberMenu(member);
        } catch (InvalidCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void memberMenu(Member member) {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n--- MEMBER MENU (" + member.getName() + ") ---");
            System.out.println("1. Search Books by Title");
            System.out.println("2. Search Books by Author");
            System.out.println("3. Search Books by Category");
            System.out.println("4. View All Books");
            System.out.println("5. Borrow Book");
            System.out.println("6. Return Book");
            System.out.println("7. View My Borrow History");
            System.out.println("8. View My Profile / Fine");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    searchByTitleFlow();
                    break;
                case 2:
                    searchByAuthorFlow();
                    break;
                case 3:
                    searchByCategoryFlow();
                    break;
                case 4:
                    viewAllBooks();
                    break;
                case 5:
                    borrowFlow(member);
                    break;
                case 6:
                    returnFlow(member);
                    break;
                case 7:
                    viewMyHistory(member);
                    break;
                case 8:
                    System.out.println(member.displayProfile());
                    break;
                case 9:
                    logout = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void registerMember() {
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Choose a Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose a Password: ");
        String password = scanner.nextLine().trim();
        try {
            Member member = library.registerMember(name, username, password);
            System.out.println("Registration successful! Your Member ID is " + member.getId());
        } catch (InvalidOperationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Admin actions
    // ---------------------------------------------------------------

    private static void addBookFlow() throws DuplicateBookException {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Total Copies: ");
        int copies = readInt();
        library.addBook(new Book(isbn, title, author, category, copies));
        System.out.println("Book added successfully.");
    }

    private static void deleteBookFlow() throws BookNotFoundException {
        System.out.print("Enter ISBN to delete: ");
        String isbn = scanner.nextLine().trim();
        library.deleteBook(isbn);
        System.out.println("Book deleted successfully.");
    }

    private static void updateBookFlow() throws BookNotFoundException {
        System.out.print("Enter ISBN to update: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("New Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("New Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("New Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("New Total Copies: ");
        int copies = readInt();
        library.updateBook(isbn, title, author, category, copies);
        System.out.println("Book updated successfully.");
    }

    private static void viewAllBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the catalog yet.");
            return;
        }
        System.out.println("\nISBN         | Title                        | Author             | Category       | Avail/Total");
        for (Book book : books) {
            System.out.println(book);
        }
    }

    private static void viewAllMembers() {
        List<Member> members = library.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No registered members yet.");
            return;
        }
        for (Member member : members) {
            System.out.println(member.displayProfile());
        }
    }

    private static void viewAllRecords() {
        List<BorrowRecord> records = library.getBorrowRecords();
        if (records.isEmpty()) {
            System.out.println("No borrow records yet.");
            return;
        }
        for (BorrowRecord record : records) {
            System.out.println(record);
        }
    }

    private static void viewOverdue() {
        List<BorrowRecord> overdue = library.getOverdueRecords();
        if (overdue.isEmpty()) {
            System.out.println("No overdue books. Great!");
            return;
        }
        for (BorrowRecord record : overdue) {
            System.out.println(record);
        }
    }

    // ---------------------------------------------------------------
    // Member actions
    // ---------------------------------------------------------------

    private static void searchByTitleFlow() {
        System.out.print("Enter title keyword: ");
        String keyword = scanner.nextLine().trim();
        printBooks(library.searchByTitle(keyword));
    }

    private static void searchByAuthorFlow() {
        System.out.print("Enter author keyword: ");
        String keyword = scanner.nextLine().trim();
        printBooks(library.searchByAuthor(keyword));
    }

    private static void searchByCategoryFlow() {
        System.out.print("Enter category keyword: ");
        String keyword = scanner.nextLine().trim();
        printBooks(library.searchByCategory(keyword));
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }

    private static void borrowFlow(Member member) {
        System.out.print("Enter ISBN to borrow: ");
        String isbn = scanner.nextLine().trim();
        try {
            library.borrowBook(member, isbn);
            System.out.println("Book borrowed successfully. Please return within 14 days.");
        } catch (LibraryException e) {
            System.out.println("Could not borrow: " + e.getMessage());
        }
    }

    private static void returnFlow(Member member) {
        System.out.print("Enter ISBN to return: ");
        String isbn = scanner.nextLine().trim();
        try {
            double fine = library.returnBook(member, isbn);
            if (fine > 0) {
                System.out.println("Book returned. Late fine incurred: Rs." + fine);
            } else {
                System.out.println("Book returned successfully. No fine due.");
            }
        } catch (LibraryException e) {
            System.out.println("Could not return: " + e.getMessage());
        }
    }

    private static void viewMyHistory(Member member) {
        List<BorrowRecord> history = library.getMemberHistory(member.getId());
        if (history.isEmpty()) {
            System.out.println("You have no borrow history yet.");
            return;
        }
        for (BorrowRecord record : history) {
            System.out.println(record);
        }
    }

    // ---------------------------------------------------------------
    // Input helper
    // ---------------------------------------------------------------

    private static int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid whole number: ");
            }
        }
    }
}
