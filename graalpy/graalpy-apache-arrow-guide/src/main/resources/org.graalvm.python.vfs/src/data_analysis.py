import pandas as pd
import pyarrow as pa

from polyglot.arrow import Float8Vector


def calculateMean(valueVector: Float8Vector) -> float:
    series = pd.Series(valueVector, dtype="float64[pyarrow]")
    return series.mean()


def calculateMedian(valueVector: Float8Vector) -> float:
    series = pd.Series(valueVector, dtype="float64[pyarrow]")
    return series.median()
