import io
import numpy as np


def mean(csv: str) -> list[float]:
    ary = np.genfromtxt(io.StringIO(csv), delimiter=",", invalid_raise=False)
    return ary.transpose().mean(axis=1).tolist()
