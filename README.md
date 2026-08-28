# library-management-system

A console library management system built on persistent CSV data.

## Project Structure
```
library-management-system
├── .gitignore
├── README.md
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
    └── generate_members.py
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
1.)  Book catalogue (title, author, ISBN, total & available copies)

2.)  Register members with unique IDs

3.)  Check-out & check-in of copies

4.)  Track loan history

5.)  Search by title, author, ISBN

6.) Persist to CSV/JSON via java.nio.file (load on start, save on exit)

7.)  Bulk-import books from CSV using parallel processing

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

## How to Run


## Sample Console Run