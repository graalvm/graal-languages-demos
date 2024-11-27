import polyglot  # pyright: ignore

from difflib import SequenceMatcher
from os import PathLike


@polyglot.export_value # ①
def compare_files(a: PathLike, b: PathLike) -> float:
    with open(a) as file_1, open(b) as file_2: 
        file1_data = file_1.read() 
        file2_data = file_2.read() 
        similarity_ratio = SequenceMatcher(None, file1_data, file2_data).ratio()
        return similarity_ratio
