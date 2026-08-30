# University Library Management System

A pure Java (no database, no external libraries) console application built for
the micro-project covering all five course outcomes:

| CO | Concept | Where it's used |
|----|---------|------------------|
| CO-1 | Classes, objects, inheritance | `Person` -> `Admin` / `Member`, `Book`, `BorrowRecord` |
| CO-2 | Polymorphism, interfaces, packages | `Manageable` / `Searchable` interfaces implemented by `Library`; overridden `getRole()`/`displayProfile()`; 6 packages |
| CO-3 | Exception handling, multithreading | 8 custom checked exceptions; `ActivityLogger` (producer-consumer thread) + `OverdueMonitor` (daemon thread) |
| CO-4 | Collection frameworks | `ConcurrentHashMap`, `CopyOnWriteArrayList`, `ArrayList`, Streams-free filtering |
| CO-5 | Data handling | Java Serialization based file persistence (`librarydata/*.dat`) so records survive a restart, without needing JDBC/servlets |

## Project structure

```
LibraryManagementSystem/
└── src/com/library/
    ├── exception/   -> LibraryException and 7 subclasses
    ├── model/       -> Person (abstract), Admin, Member, Book, BorrowRecord
    ├── interfaces/  -> Manageable, Searchable
    ├── util/        -> IdGenerator, DataPersistence
    ├── service/      -> Library (singleton core), ActivityLogger, OverdueMonitor
    └── main/        -> LibraryManagementSystem (console UI, entry point)
```

## How to compile and run

Requires JDK 8 or later.

```bash
cd LibraryManagementSystem
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
cd bin
java com.library.main.LibraryManagementSystem
```

A `librarydata/` folder is created automatically next to wherever you run the
`java` command from, holding the serialized catalog/members/records and an
`activity.log` audit trail. Delete that folder to reset the system to a clean
slate.

## Default login

- **Admin** — username: `admin`, password: `admin123`
- **Member** — register a new account from the main menu (option 3)

## Features

**Admin**
- Add / delete / update books (ISBN, title, author, category, copies)
- View full catalog, all members, all borrow records, and currently overdue books

**Member**
- Search by title / author / category
- Borrow a book (max 3 at a time, blocked if no copies left)
- Return a book (auto-calculates a Rs.5/day late fine after the 14-day loan period)
- View personal borrow history and profile/fine summary

**Behind the scenes**
- A background daemon thread scans for overdue books every 15 seconds and
  writes a note to the activity log — a live multithreading demo you can see
  in `librarydata/activity.log` if the app is left running.
- A separate logging thread consumes a `BlockingQueue` so no menu action ever
  blocks on disk I/O (classic producer-consumer pattern).
- All borrow/return operations are `synchronized` so the logic stays correct
  even under concurrent access.
- On exit (including via a JVM shutdown hook, so it's safe even if the
  process is killed), all data is serialized to disk and reloaded
  automatically next time the program starts.

## Notes for your report

This satisfies "final code in Java only" per your request — no database
driver, servlet container, or JSP is used anywhere; persistence is handled
with plain `java.io` object serialization, which still lets data survive
across runs for your demo.
