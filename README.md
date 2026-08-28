# library-management-system

A console library management system built on persistent CSV data.

## Data source

The raw book data (`books.csv`, not tracked in this repo due to size) comes from:

**Best Books Ever Dataset** — [https://zenodo.org/records/4265096](https://zenodo.org/records/4265096)

Download it and place it in this folder as `books.csv` before running the scripts below.

## Progress so far

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
- `total_copies`: random single-digit value (1-9)
- `available_copies`: random value between 0 and `total_copies` (never exceeds it)

Output: `books_inventory.csv` (columns: `bookId, total_copies, available_copies`)

### `generate_loans.py`
Generates `books_loans.csv` from `books_inventory.csv` — the loan history for each book:
- One `Borrowed` (active) loan row per currently checked-out copy (`total_copies - available_copies`), with checkout dates skewed recent so most active loans aren't overdue yet
- 0-3 historical `Returned` loan rows per book, with return dates skewed toward being on time
- `due_date` is `checkout_date + 14 days` (`LOAN_PERIOD_DAYS`) for every loan
- `is_overdue`, `overdue_days`, and `penalty_amount` (`overdue_days * $0.50/day`) are derived for both `Borrowed` (checked against today) and `Returned` (checked against `checkin_date`) loans
- Dummy `memberId` drawn from a pool of 300 possible members

Output: `books_loans.csv` (columns: `loanId, bookId, memberId, checkout_date, due_date, checkin_date, status, overdue_days, is_overdue, penalty_amount`)

## Planned next steps
- `members.csv` — dummy member records (`memberId, member_name`)