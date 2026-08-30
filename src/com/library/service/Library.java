package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotBorrowedException;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.exception.InvalidCredentialsException;
import com.library.exception.InvalidOperationException;
import com.library.exception.MembershipLimitExceededException;
import com.library.interfaces.Manageable;
import com.library.interfaces.Searchable;
import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.util.DataPersistence;
import com.library.util.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central service class of the application. Implements both
 * {@link Manageable} (admin operations) and {@link Searchable} (member
 * operations), so a single object is referenced polymorphically through
 * either interface type depending on who is using it.
 *
 * Implemented as a classic singleton so the whole console application
 * shares one in-memory catalog. Uses concurrent collection types and
 * synchronized methods so borrow/return operations remain correct even if
 * called from multiple threads at once.
 */
public class Library implements Manageable, Searchable {

    private static Library instance;

    private final Map<String, Book> catalog = new ConcurrentHashMap<String, Book>();
    private final Map<String, Member> membersByUsername = new ConcurrentHashMap<String, Member>();
    private final Map<String, Admin> adminsByUsername = new ConcurrentHashMap<String, Admin>();
    private final List<BorrowRecord> borrowRecords = new CopyOnWriteArrayList<BorrowRecord>();

    private final ActivityLogger logger = new ActivityLogger();
    private Thread loggerThread;
    private OverdueMonitor overdueMonitor;

    private Library() {
        seedDefaultAdminIfNeeded();
    }

    public static synchronized Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    private void seedDefaultAdminIfNeeded() {
        if (adminsByUsername.isEmpty()) {
            adminsByUsername.put("admin",
                    new Admin("A001", "Default Administrator", "admin", "admin123", "EMP001"));
        }
    }

    // ---------------------------------------------------------------
    // Background services (multithreading)
    // ---------------------------------------------------------------

    public void startBackgroundServices() {
        loggerThread = new Thread(logger, "ActivityLogger");
        loggerThread.start();
        overdueMonitor = new OverdueMonitor(this, 15000L); // scan every 15 seconds
        overdueMonitor.start();
        logger.log("Library system started.");
    }

    public void shutdownBackgroundServices() {
        logger.log("Library system shutting down.");
        if (overdueMonitor != null) {
            overdueMonitor.stopMonitoring();
        }
        logger.stop();
        try {
            if (loggerThread != null) {
                loggerThread.join(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ActivityLogger getLogger() {
        return logger;
    }

    // ---------------------------------------------------------------
    // Manageable (Admin operations)
    // ---------------------------------------------------------------

    @Override
    public void addBook(Book book) throws DuplicateBookException {
        if (catalog.containsKey(book.getIsbn())) {
            throw new DuplicateBookException("A book with ISBN " + book.getIsbn() + " already exists.");
        }
        catalog.put(book.getIsbn(), book);
        logger.log("Book added: '" + book.getTitle() + "' (" + book.getIsbn() + ")");
    }

    @Override
    public void deleteBook(String isbn) throws BookNotFoundException {
        Book removed = catalog.remove(isbn);
        if (removed == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        logger.log("Book deleted: '" + removed.getTitle() + "' (" + isbn + ")");
    }

    @Override
    public void updateBook(String isbn, String newTitle, String newAuthor, String newCategory, int newTotalCopies)
            throws BookNotFoundException {
        Book book = catalog.get(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        int currentlyBorrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (newTotalCopies < currentlyBorrowed) {
            // Can never drop below the number of copies presently out on loan.
            newTotalCopies = currentlyBorrowed;
        }
        int delta = newTotalCopies - book.getTotalCopies();
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setCategory(newCategory);
        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(book.getAvailableCopies() + delta);
        logger.log("Book updated: " + isbn);
    }

    // ---------------------------------------------------------------
    // Searchable (Member operations)
    // ---------------------------------------------------------------

    @Override
    public List<Book> searchByTitle(String keyword) {
        List<Book> results = new ArrayList<Book>();
        String needle = keyword.toLowerCase();
        for (Book book : catalog.values()) {
            if (book.getTitle().toLowerCase().contains(needle)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByAuthor(String keyword) {
        List<Book> results = new ArrayList<Book>();
        String needle = keyword.toLowerCase();
        for (Book book : catalog.values()) {
            if (book.getAuthor().toLowerCase().contains(needle)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByCategory(String keyword) {
        List<Book> results = new ArrayList<Book>();
        String needle = keyword.toLowerCase();
        for (Book book : catalog.values()) {
            if (book.getCategory().toLowerCase().contains(needle)) {
                results.add(book);
            }
        }
        return results;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<Book>(catalog.values());
    }

    // ---------------------------------------------------------------
    // Membership
    // ---------------------------------------------------------------

    public Member registerMember(String name, String username, String password) throws InvalidOperationException {
        if (membersByUsername.containsKey(username)) {
            throw new InvalidOperationException("Username '" + username + "' is already taken.");
        }
        String id = IdGenerator.nextMemberId();
        Member member = new Member(id, name, username, password);
        membersByUsername.put(username, member);
        logger.log("New member registered: " + name + " (" + id + ")");
        return member;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<Member>(membersByUsername.values());
    }

    // ---------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------

    public Admin authenticateAdmin(String username, String password) throws InvalidCredentialsException {
        Admin admin = adminsByUsername.get(username);
        if (admin == null || !admin.checkPassword(password)) {
            throw new InvalidCredentialsException("Invalid admin username or password.");
        }
        return admin;
    }

    public Member authenticateMember(String username, String password) throws InvalidCredentialsException {
        Member member = membersByUsername.get(username);
        if (member == null || !member.checkPassword(password)) {
            throw new InvalidCredentialsException("Invalid member username or password.");
        }
        return member;
    }

    // ---------------------------------------------------------------
    // Borrow / Return (thread-safe critical sections)
    // ---------------------------------------------------------------

    public synchronized BorrowRecord borrowBook(Member member, String isbn)
            throws BookNotFoundException, BookNotAvailableException, MembershipLimitExceededException {
        Book book = catalog.get(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("'" + book.getTitle() + "' has no copies available right now.");
        }
        if (!member.canBorrowMore()) {
            throw new MembershipLimitExceededException(
                    "Borrow limit of " + Member.MAX_BOOKS_ALLOWED + " books reached. Return a book first.");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        member.getBorrowedIsbns().add(isbn);
        BorrowRecord record = new BorrowRecord(IdGenerator.nextRecordId(), member.getId(), isbn);
        borrowRecords.add(record);
        logger.log(member.getName() + " borrowed '" + book.getTitle() + "'");
        return record;
    }

    public synchronized double returnBook(Member member, String isbn)
            throws BookNotFoundException, BookNotBorrowedException {
        Book book = catalog.get(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book found with ISBN " + isbn);
        }
        if (!member.getBorrowedIsbns().contains(isbn)) {
            throw new BookNotBorrowedException("You have not borrowed '" + book.getTitle() + "'.");
        }

        BorrowRecord activeRecord = null;
        for (BorrowRecord record : borrowRecords) {
            if (record.getMemberId().equals(member.getId())
                    && record.getIsbn().equals(isbn)
                    && !record.isReturned()) {
                activeRecord = record;
                break;
            }
        }
        if (activeRecord == null) {
            throw new BookNotBorrowedException("No active borrow record found for this book.");
        }

        double fine = activeRecord.markReturned();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        member.getBorrowedIsbns().remove(isbn);
        if (fine > 0) {
            member.addFine(fine);
        }
        logger.log(member.getName() + " returned '" + book.getTitle() + "'"
                + (fine > 0 ? (" with a fine of Rs." + fine) : ""));
        return fine;
    }

    public List<BorrowRecord> getBorrowRecords() {
        return new ArrayList<BorrowRecord>(borrowRecords);
    }

    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> overdue = new ArrayList<BorrowRecord>();
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue()) {
                overdue.add(record);
            }
        }
        return overdue;
    }

    public List<BorrowRecord> getMemberHistory(String memberId) {
        List<BorrowRecord> history = new ArrayList<BorrowRecord>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getMemberId().equals(memberId)) {
                history.add(record);
            }
        }
        return history;
    }

    // ---------------------------------------------------------------
    // Persistence (plain Java file I/O via serialization - no DB)
    // ---------------------------------------------------------------

    public void saveData() {
        DataPersistence.save("catalog.dat", new HashMap<String, Book>(catalog));
        DataPersistence.save("members.dat", new HashMap<String, Member>(membersByUsername));
        DataPersistence.save("admins.dat", new HashMap<String, Admin>(adminsByUsername));
        DataPersistence.save("records.dat", new ArrayList<BorrowRecord>(borrowRecords));
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        Map<String, Book> loadedCatalog = DataPersistence.load("catalog.dat", HashMap.class);
        if (loadedCatalog != null) {
            catalog.putAll(loadedCatalog);
        }

        Map<String, Member> loadedMembers = DataPersistence.load("members.dat", HashMap.class);
        if (loadedMembers != null) {
            membersByUsername.putAll(loadedMembers);
            int highest = 1000;
            for (Member m : loadedMembers.values()) {
                highest = Math.max(highest, safeParseSuffix(m.getId(), "MEM"));
            }
            IdGenerator.fastForwardMemberCounter(highest);
        }

        Map<String, Admin> loadedAdmins = DataPersistence.load("admins.dat", HashMap.class);
        if (loadedAdmins != null && !loadedAdmins.isEmpty()) {
            adminsByUsername.clear();
            adminsByUsername.putAll(loadedAdmins);
        }

        List<BorrowRecord> loadedRecords = DataPersistence.load("records.dat", ArrayList.class);
        if (loadedRecords != null) {
            borrowRecords.addAll(loadedRecords);
            int highest = 0;
            for (BorrowRecord r : loadedRecords) {
                highest = Math.max(highest, safeParseSuffix(r.getRecordId(), "REC"));
            }
            IdGenerator.fastForwardRecordCounter(highest + 1);
        }
    }

    private int safeParseSuffix(String value, String prefix) {
        try {
            return Integer.parseInt(value.replace(prefix, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
