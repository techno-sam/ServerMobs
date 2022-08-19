"""
Recommends entities to use as client-side entity to match a specified size.
"""

import typing
import json


name_dim = json.load(open("hitboxes.json"))

goal_width = float(input("Target width: "))
goal_height = float(input("Target height: "))

"""lower is better"""
def score_bounds(width: float, height: float) -> float:
    return abs(goal_width-width) + abs(goal_height-height)


def score_bounds_name(name: str) -> float:
    bounds = name_dim[name]
    return score_bounds(*bounds)

names = list(name_dim.keys())
names.sort(key=score_bounds_name)

for i in range(10):
    name = names[i]
    dim = name_dim[name]
    print(f"{i+1}. {name}: {dim}")
