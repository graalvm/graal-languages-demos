import io
import numpy as np
import pandas as pd
import threading


def calculateMean(csv: str, index: int) -> float:
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].mean())


def calculateMedian(csv: str, index: int) -> float:
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].median())


def describe(csv: str) -> str:
    df = pd.read_csv(io.StringIO(csv), header=None)
    return repr(df.describe())


print("Pandas ready on thread", threading.get_native_id())
