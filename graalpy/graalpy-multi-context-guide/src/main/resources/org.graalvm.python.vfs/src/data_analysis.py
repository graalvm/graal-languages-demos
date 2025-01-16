import io
import numpy as np
import pandas as pd
import threading


def calculateMean(csv: str, index: int):
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].mean())


def calculateMedian(csv: str, index: int):
    df = pd.read_csv(io.StringIO(csv), header=None)
    return float(df.loc[:, index].median())


print("Pandas armed and ready on thread", threading.get_native_id())
