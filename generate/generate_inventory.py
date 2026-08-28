import polars as pl
from pathlib import Path
import random

def generate_inventory(catalogue_path: str, output_path: str, seed: int = 42) -> None:
    rng = random.Random(seed)
    df = pl.read_csv(catalogue_path)
    total_copies = [rng.randint(1, 9) for _ in range(df.height)]
    available_copies = [rng.randint(0, total) for total in total_copies]
    inventory = pl.DataFrame({
        "bookId": df["bookId"],
        "total_copies": total_copies,
        "available_copies": available_copies,
    })
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    inventory.write_csv(output_path)

if __name__ == "__main__":
    data_dir = Path(__file__).parent.parent / "data"
    generate_inventory(data_dir / "books_catalogue.csv", data_dir / "books_inventory.csv")
