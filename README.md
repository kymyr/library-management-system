# library-management-system

A console library management system built on persistent CSV data.
Given the requirements, this is in the perspective of a librarian / administrator and not a member / user.

## Project Structure
```
library-management-system
├── .gitignore
├── README.md
├── console_library_management_system
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── librarymanagement
│       │           ├── cli
│       │           │   ├── ConsoleDisplay.java
│       │           │   ├── ConsoleInput.java
│       │           │   ├── Library.java
│       │           │   └── MenuDisplay.java
│       │           ├── exception
│       │           │   ├── BookNotFoundException.java
│       │           │   ├── BookUnavailableException.java
│       │           │   ├── InvalidInputException.java
│       │           │   ├── LibraryException.java
│       │           │   └── MemberNotFoundException.java
│       │           ├── model
│       │           │   ├── Book.java
│       │           │   ├── Loan.java
│       │           │   ├── LoanStatus.java
│       │           │   └── Member.java
│       │           ├── repository
│       │           │   ├── BookRepository.java
│       │           │   ├── LoanRepository.java
│       │           │   └── MemberRepository.java
│       │           ├── service
│       │           │   ├── BookCsvReader.java
│       │           │   ├── LoanCsvReader.java
│       │           │   └── MemberCsvReader.java
│       │           └── util
│       │               └── ErrorHandling.java
│       └── test
│           └── java
│               └── librarymanagement
│                   ├── cli
│                   │   ├── ConsoleDisplayTest.java
│                   │   ├── ConsoleInputTest.java
│                   │   ├── LibraryTest.java
│                   │   └── MenuDisplayTest.java
│                   ├── model
│                   │   ├── BookTest.java
│                   │   ├── LoanTest.java
│                   │   └── MemberTest.java
│                   ├── repository
│                   │   ├── BookRepositoryTest.java
│                   │   ├── LoanRepositoryTest.java
│                   │   └── MemberRepositoryTest.java
│                   ├── service
│                   │   ├── CsvServiceConcurrencyTest.java
│                   │   ├── CsvServiceTest.java
│                   │   ├── LoanServiceTest.java
│                   │   └── MemberServiceTest.java
│                   └── util
│                       └── ErrorHandlingTest.java
├── data
│   ├── books.csv
│   ├── books_catalogue.csv
│   ├── books_inventory.csv
│   ├── books_loans.csv
│   └── library_members.csv
└── generate_library_catalogue
    ├── clean_csv.py
    ├── generate_inventory.py
    ├── generate_loans.py
    ├── generate_members.py
    └── requirements.txt
```
---

## Data source

The raw book data (`books.csv`, not tracked in this repo due to size) comes from:

**Best Books Ever Dataset** — [https://zenodo.org/records/4265096](https://zenodo.org/records/4265096)

Download it and place it in this folder as `books.csv` before running the scripts below.

## Generate Library Catalogue

### `clean_csv.py`
Builds `books_catalogue.csv` from the raw `books.csv`, keeping only `bookId, title, author, isbn`. Cleaning steps applied:
- Filters to `language == "English"` rows only
- Handles inconsistent numeric columns via `ignore_errors=True`
- Extracts the numeric prefix of `bookId` (e.g. `"2767052-the-hunger-games"` → `2767052`) and dedupe
- Keeps only rows where `isbn` is fully numeric, and dedupe
- Strips role annotations like `(Illustrator)`, `(Editor)` from `author`
- Drops truncated `"more…"` entries and non-Latin-script names from `author`, and flattens accented Latin characters (e.g. `é` → `e`)
- Drops rows with corrupted/mojibake titles or fully non-Latin-script titles, and strips stray stylized symbols (e.g. `★`, `™`, `→`) from the rest

Output: `books_catalogue.csv` (columns: `bookId, title, author, isbn`)

### `generate_inventory.py`
Generates `books_inventory.csv` from `books_catalogue.csv` with dummy copy counts for each book:
- `total_copies`: random value between 1 and 3
- `available_copies`: added later by `generate_loans.py` based on active loans

Output: `books_inventory.csv` (columns: `bookId, total_copies` initially; `available_copies` is added by `generate_loans.py`)

### `generate_members.py`
Generates `library_members.csv` with 500 dummy library members. Each member includes:
- A generated first name, last name, and email address
- A join date between 30 and 2,000 days ago
- A membership expiry date based on yearly renewals
- A `membership_status` of `Active` or `Expired`

Output: `library_members.csv` (columns: `memberId, first_name, last_name, email, join_date, membership_expiry_date, membership_status`)

### `generate_loans.py`
Generates `books_loans.csv` from `books_inventory.csv` — the loan history for each book:
- One `Borrowed` (active) loan row per currently checked-out copy (`total_copies - available_copies`), with checkout dates skewed recent so most active loans aren't overdue yet
- 0-2 historical `Returned` loan rows per book, with return dates skewed toward being on time
- `due_date` is `checkout_date + 14 days` (`LOAN_PERIOD_DAYS`) for every loan
- `is_overdue`, `overdue_days`, and `penalty_amount` (`overdue_days * $0.50/day`) are derived for both `Borrowed` (checked against today) and `Returned` (checked against `checkin_date`) loans
- Dummy `memberId` drawn from a pool of 500 possible members

Output: `books_loans.csv` (columns: `loanId, bookId, memberId, checkout_date, due_date, checkin_date, status, overdue_days, is_overdue, penalty_amount`)

## How to generate

Install dependencies:

```bash
pip install -r generate_library_catalogue/requirements.txt
```

Run the scripts in this order from the `generate_library_catalogue` directory:

```bash
python generate_library_catalogue/clean_csv.py
python generate_library_catalogue/generate_inventory.py
python generate_library_catalogue/generate_members.py
python generate_library_catalogue/generate_loans.py
```

Each script reads the output created by the previous step. The final step also updates `books_inventory.csv` with `available_copies`, based on the active loans it creates.

The generated files are written to the `data/` directory:

1. `books_catalogue.csv`
2. `books_inventory.csv`
3. `library_members.csv`
4. `books_loans.csv`

---

## Console Library Management System

### Feature Requirements
1.) Book catalogue (title, author, ISBN, total & available copies)

2.) Register members with unique IDs

3.) Check-out & check-in of copies

4.) Track loan history

5.) Search by title, author, ISBN

6.) Persist to CSV/JSON via java.nio.file (load on start, save on exit)

7.) Bulk-import books from CSV using parallel processing

8.) Clear CLI menu with helpful validation & error messages

### Mandatory Requirements
-  ≥ 8 classes in clear packages
- ≥ 1 interface, 1 abstract class, 1 record, 1 enum
- Custom exception hierarchy (≥ 3 specific types)
- Meaningful collections, generics, streams
- JUnit 5 suite, ≥ 75% line coverage incl. failure paths
- ≥ 1 race-safe concurrency feature (ExecutorService / CompletableFuture)
- README with setup + short architecture diagram
- Maven build, portable relative paths — no secrets / no network

### Testing & coverage
- JUnit 5 (+ AssertJ, Mockito - both optional)
- Cover validation, search, failure paths
- Persistence round-trip; malformed input
- Concurrent-import correctness
- Use @TempDir —never real user files
- ≥ 75% line coverage via JaCoCo (mvn clean verify)

### Concurrency

When the library starts, book records are imported from the catalogue CSV using several workers at the same time. Each worker prepares book data independently, and the completed results are then added to the library catalogue.

This reduces the time needed to load the large book dataset. The import also reports how many books were imported successfully and how many rows could not be processed. If the import is interrupted, the workers are stopped and the system restores the interruption status before reporting the failure.

### Exceptions

The system uses clear error types to handle common problems without stopping the program. When an error occurs, the librarian sees a helpful message and can continue using the menu.

- **Book not found:** the entered book ID does not exist in the catalogue.
- **Member not found:** the entered member ID is not registered.
- **Book unavailable:** the book exists, but all copies are currently borrowed.
- **Invalid input:** the entered value is not in the expected format, such as text entered where an ID is required.

These errors are organised under one general library error type so they can be handled consistently across the system.


## Development Roadmap WIP / Plan
#### W1
Day 1 - Generate Library Catalogue
- Includes books, stocks, loans, members info in csvs

#### W2
Day 2 - Build Maven Structure
- Create a basic cli menu with no connected functionality
- Add basic members and books repository info
- Add basic exception handling
- Add static values testing 

Day 3 - CSV integration (read-only)
- Integrate generated datasets into console library management system (read-only functionalities for now)
- add loans menu
- connect basic read-only functionalities, no concurrency for now
  - list all books
  - search books by title, author, isbm
  - show borrowed books 
  - search loan history by bookId or memberId

Day 4 - Persistent write csv integration, some refactoring
- register new member
- check-in / check-out book
- persist details in csv data
- refactor cli

#### W3
Day 5 - Concurrency & exceptions
 - concurrency for csv imports
 - create exceptions hierarchy first

Day 6 - Exceptions integration

Day 7 - jUnit testing
 - junit tests
 - configure jacoco (ended at 26% coverage on first run)

#### W4
Day 8 - junit config (cont...)
- add tests to reach required JaCoCo coverage

Day 9 - update documentation specs & build testing from fresh clone

Day 10 - draft presentation flow

## How to Run


## Sample Console Run


## Testing
Run the JUnit 5 test suite with JaCoCo from the Maven module:

```
cd console_library_management_system
mvn clean verify
```

## Possible improvements in the future
- Add filter options when searching
- Add new entries for library catalogue
- membership renewal