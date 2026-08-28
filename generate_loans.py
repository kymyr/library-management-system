import polars as pl
from pathlib import Path
import random
from datetime import date, timedelta

MEMBER_POOL_SIZE = 300
MAX_HISTORICAL_RETURNS_PER_BOOK = 3
LOAN_PERIOD_DAYS = 14
PENALTY_PER_DAY = 0.50

def generate_loans(inventory_path: str, output_path: str, seed: int = 42) -> None:
    rng = random.Random(seed)
    inventory = pl.read_csv(inventory_path)
    today = date.today()

    loan_id = 1
    rows = []
    for book_id, total, available in inventory.iter_rows():
        borrowed_count = total - available
        for _ in range(borrowed_count):
            # skew toward recent checkouts so most active loans aren't overdue yet
            days_since_checkout = round(rng.triangular(1, 20, 6))
            checkout_date = today - timedelta(days=days_since_checkout)
            due_date = checkout_date + timedelta(days=LOAN_PERIOD_DAYS)
            rows.append({
                "loanId": loan_id,
                "bookId": book_id,
                "memberId": rng.randint(1, MEMBER_POOL_SIZE),
                "checkout_date": checkout_date.isoformat(),
                "due_date": due_date.isoformat(),
                "checkin_date": None,
                "status": "Borrowed",
            })
            loan_id += 1

        # past loans that have already been returned, independent of current copy counts
        for _ in range(rng.randint(0, MAX_HISTORICAL_RETURNS_PER_BOOK)):
            checkout_date = today - timedelta(days=rng.randint(30, 365))
            due_date = checkout_date + timedelta(days=LOAN_PERIOD_DAYS)
            # skew toward returning on/before the due date; only a minority run late
            days_to_return = round(rng.triangular(1, 18, 9))
            checkin_date = checkout_date + timedelta(days=days_to_return)
            rows.append({
                "loanId": loan_id,
                "bookId": book_id,
                "memberId": rng.randint(1, MEMBER_POOL_SIZE),
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

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    loans.write_csv(output_path)

if __name__ == "__main__":
    script_dir = Path(__file__).parent
    generate_loans(script_dir / "books_inventory.csv", script_dir / "books_loans.csv")
