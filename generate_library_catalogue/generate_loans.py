import polars as pl
from pathlib import Path
import random
from datetime import date, timedelta

MAX_HISTORICAL_RETURNS_PER_BOOK = 2
LOAN_PERIOD_DAYS = 14
PENALTY_PER_DAY = 0.50
# how many books an active member can realistically have checked out at once
CONCURRENT_LOAN_WEIGHTS = {0: 40, 1: 30, 2: 15, 3: 10, 4: 4, 5: 1}

def generate_loans(inventory_path: str, members_path: str, loans_output_path: str, seed: int = 42) -> None:
    rng = random.Random(seed)
    inventory = pl.read_csv(inventory_path)
    members = pl.read_csv(members_path)
    today = date.today()

    active_member_ids = members.filter(pl.col("membership_status") == "Active")["memberId"].to_list()
    all_member_ids = members["memberId"].to_list()

    # remaining capacity per book, consumed as active loans are assigned to specific copies
    remaining_capacity = dict(zip(inventory["bookId"].to_list(), inventory["total_copies"].to_list()))
    book_ids = list(remaining_capacity.keys())

    loan_id = 1
    rows = []

    # active loans: only Active members can currently hold a book, capped per member
    for member_id in active_member_ids:
        loan_count = rng.choices(
            list(CONCURRENT_LOAN_WEIGHTS.keys()), weights=list(CONCURRENT_LOAN_WEIGHTS.values())
        )[0]
        for _ in range(loan_count):
            # pick a book that still has a copy free; give up after a few tries to avoid an infinite loop
            for _ in range(10):
                book_id = rng.choice(book_ids)
                if remaining_capacity[book_id] > 0:
                    remaining_capacity[book_id] -= 1
                    break
            else:
                continue

            # skew toward recent checkouts so most active loans aren't overdue yet
            days_since_checkout = round(rng.triangular(1, 20, 6))
            checkout_date = today - timedelta(days=days_since_checkout)
            due_date = checkout_date + timedelta(days=LOAN_PERIOD_DAYS)
            rows.append({
                "loanId": loan_id,
                "bookId": book_id,
                "memberId": member_id,
                "checkout_date": checkout_date.isoformat(),
                "due_date": due_date.isoformat(),
                "checkin_date": None,
                "status": "Borrowed",
            })
            loan_id += 1

    # historical returned loans: any member (active or expired) may have borrowed in the past
    for book_id in book_ids:
        for _ in range(rng.randint(0, MAX_HISTORICAL_RETURNS_PER_BOOK)):
            checkout_date = today - timedelta(days=rng.randint(30, 365))
            due_date = checkout_date + timedelta(days=LOAN_PERIOD_DAYS)
            # skew toward returning on/before the due date; only a minority run late
            days_to_return = round(rng.triangular(1, 18, 9))
            checkin_date = checkout_date + timedelta(days=days_to_return)
            rows.append({
                "loanId": loan_id,
                "bookId": book_id,
                "memberId": rng.choice(all_member_ids),
                "checkout_date": checkout_date.isoformat(),
                "due_date": due_date.isoformat(),
                "checkin_date": checkin_date.isoformat(),
                "status": "Returned",
            })
            loan_id += 1

    loans = pl.DataFrame(rows, schema={
        "loanId": pl.Int64,
        "bookId": pl.Int64,
        "memberId": pl.Int64,
        "checkout_date": pl.String,
        "due_date": pl.String,
        "checkin_date": pl.String,
        "status": pl.String,
    })

    # reference_date is checkin_date for returned loans, else today for loans still out
    loans = loans.with_columns(
        pl.col("checkin_date").fill_null(today.isoformat()).str.to_date().alias("_reference_date"),
        pl.col("due_date").str.to_date().alias("_due_date"),
    )
    loans = loans.with_columns(
        (pl.col("_reference_date") - pl.col("_due_date")).dt.total_days().clip(lower_bound=0).alias("overdue_days")
    )
    loans = loans.with_columns(
        (pl.col("overdue_days") > 0).alias("is_overdue"),
        (pl.col("overdue_days") * PENALTY_PER_DAY).alias("penalty_amount"),
    )
    loans = loans.drop("_reference_date", "_due_date")

    Path(loans_output_path).parent.mkdir(parents=True, exist_ok=True)
    loans.write_csv(loans_output_path)

    # available_copies is derived from how much capacity active loans actually consumed
    inventory = inventory.with_columns(
        pl.col("bookId").replace_strict(remaining_capacity, default=None).alias("available_copies")
    )
    inventory.write_csv(inventory_path)

if __name__ == "__main__":
    data_dir = Path(__file__).parent.parent / "data"
    generate_loans(
        data_dir / "books_inventory.csv",
        data_dir / "library_members.csv",
        data_dir / "books_loans.csv",
    )
