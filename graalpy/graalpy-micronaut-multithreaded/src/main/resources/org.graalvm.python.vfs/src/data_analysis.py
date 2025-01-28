import io
import numpy as np
import pandas as pd
import threading


ID = repr(object()).rsplit(" ", 1)[-1][:-2].ljust(10)


def calculateMean(csv: str, index: int) -> float:
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].mean())


def calculateMedian(csv: str, index: int) -> float:
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].median())


def describe(csv: str) -> str:
    # print(ID, "on thread", threading.get_native_id())
    df = pd.read_csv(io.StringIO(csv), header=None)
    return repr(df.describe())


print("Pandas", ID, "ready on thread", threading.get_native_id())
