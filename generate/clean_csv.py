import polars as pl
from pathlib import Path
import unicodedata

COLUMNS = ["bookId", "title", "author", "isbn"]

def _clean_author_names(author: str) -> str:
    names = [n.strip() for n in author.split(",")]
    # drop truncated "more…" entries and names with non-Latin script characters
    names = [n for n in names if n.rstrip(".…").lower() != "more"]
    names = [n for n in names if not any(
        ord(c) > 0x2FF and not unicodedata.category(c).startswith("P") for c in n
    )]
    # flatten remaining accented Latin characters (é, ï, ż, etc.) to plain ASCII
    normalized = []
    for n in names:
        decomposed = unicodedata.normalize("NFKD", n)
        normalized.append("".join(c for c in decomposed if not unicodedata.combining(c)))
    return ", ".join(normalized)

# unicode blocks for scripts we don't want mixed into "English" titles
NON_LATIN_SCRIPT_RE = r"[\u0400-\u04FF\u0600-\u06FF\u0980-\u09FF\u3040-\u30FF\u4E00-\u9FFF]"

def _clean_title(title: str) -> str:
    # strip stray symbol characters like ★, ™, →, ● while keeping normal punctuation
    return "".join(c for c in title if not unicodedata.category(c).startswith("S")).strip()

def clean_csv(file_path: str, output_path: str) -> None:
    # ignore_errors avoids failures from mixed-type values like "1 page" in numeric columns
    df = pl.read_csv(file_path, ignore_errors=True)
    print(df.schema)
    df = df.filter(pl.col("language") == "English")
    df = df.select(COLUMNS)
    df = df.drop_nulls()
    df = df.filter(pl.col("isbn").str.contains(r"^\d+$"))
    # bookId is like "2767052-the-hunger-games"; keep only the leading numeric id
    df = df.with_columns(
        pl.col("bookId").str.extract(r"^(\d+)", 1).cast(pl.Int64)
    )
    df = df.unique(subset=["bookId"], keep="first")

    # strip role annotations like "(Illustrator)" from author names
    df = df.with_columns(
        pl.col("author").str.replace_all(r"\s*\([^)]*\)", "").str.strip_chars()
    )

    # drop non-Latin-script author names, normalize accented Latin ones to plain ASCII
    df = df.with_columns(
        pl.col("author").map_elements(_clean_author_names, return_dtype=pl.String)
    )
    df = df.filter(pl.col("author") != "")

    # drop rows with corrupted encoding (replacement char) or non-Latin script titles
    df = df.filter(~pl.col("title").str.contains("\ufffd"))
    df = df.filter(~pl.col("title").str.contains(NON_LATIN_SCRIPT_RE))
    # strip stray stylized symbols (★, ™, →, ●) from remaining titles
    df = df.with_columns(
        pl.col("title").map_elements(_clean_title, return_dtype=pl.String)
    )
    df = df.filter(pl.col("title") != "")

    # drop duplicate ISBNs, keeping the first occurrence
    df = df.unique(subset=["isbn"], keep="first")

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    df.write_csv(output_path)

if __name__ == "__main__":
    data_dir = Path(__file__).parent.parent / "data"
    clean_csv(data_dir / "books.csv", data_dir / "books_catalogue.csv")