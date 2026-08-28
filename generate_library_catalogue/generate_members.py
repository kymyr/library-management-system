import polars as pl
from pathlib import Path
import random
from datetime import date, timedelta

MEMBER_COUNT = 500
MEMBERSHIP_PERIOD_DAYS = 365
RENEWAL_PROBABILITY = 0.85  # chance a member renews at each yearly anniversary

FIRST_NAMES = [
    "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
    "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
    "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
    "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
    "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle",
]
LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
    "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
]

def generate_members(output_path: str, seed: int = 42) -> None:
    rng = random.Random(seed)
    today = date.today()

    rows = []
    for member_id in range(1, MEMBER_COUNT + 1):
        join_date = today - timedelta(days=rng.randint(30, 2000))

        # membership renews yearly; each anniversary has a high chance of renewing,
        # so lapsed members are the minority rather than the norm for long-time members
        renewal_periods_elapsed = (today - join_date).days // MEMBERSHIP_PERIOD_DAYS
        expiry_date = join_date + timedelta(days=MEMBERSHIP_PERIOD_DAYS)
        for _ in range(renewal_periods_elapsed):
            if rng.random() > RENEWAL_PROBABILITY:
                break
            expiry_date += timedelta(days=MEMBERSHIP_PERIOD_DAYS)
        status = "Active" if expiry_date >= today else "Expired"

        first_name = rng.choice(FIRST_NAMES)
        last_name = rng.choice(LAST_NAMES)
        rows.append({
            "memberId": member_id,
            "first_name": first_name,
            "last_name": last_name,
            "email": f"{first_name.lower()}.{last_name.lower()}{member_id}@example.com",
            "join_date": join_date.isoformat(),
            "membership_expiry_date": expiry_date.isoformat(),
            "membership_status": status,
        })

    members = pl.DataFrame(rows, schema={
        "memberId": pl.Int64,
        "first_name": pl.String,
        "last_name": pl.String,
        "email": pl.String,
        "join_date": pl.String,
        "membership_expiry_date": pl.String,
        "membership_status": pl.String,
    })

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    members.write_csv(output_path)

if __name__ == "__main__":
    data_dir = Path(__file__).parent.parent / "data"
    generate_members(data_dir / "library_members.csv")
