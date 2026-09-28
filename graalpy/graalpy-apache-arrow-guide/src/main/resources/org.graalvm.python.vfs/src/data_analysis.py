import pandas as pd
from polyglot.arrow import Float8Vector, enable_java_integration

enable_java_integration() # ①

def calculateMean(valueVector: Float8Vector) -> float:
    series = pd.Series(valueVector, dtype="float64[pyarrow]")
    return series.mean()


def calculateMedian(valueVector: Float8Vector) -> float:
    series = pd.Series(valueVector, dtype="float64[pyarrow]")
    return series.median()
