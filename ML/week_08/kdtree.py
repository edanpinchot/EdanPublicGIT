# -*- coding: utf-8 -*-
"""KDTree.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1MygWjn5MilCXt3Pp1S1ygIfn1LoUxh2A
"""

#import california housing dataset
from sklearn.datasets import fetch_california_housing
dataset = fetch_california_housing()
data = dataset.data

#make the KD-Tree
def make_kd_tree(points, dim, i=0):

    if len(points) > 1:
        sorted_points = sorted(points, key=lambda point: point[i])
        i = (i + 1) % dim
        half = len(sorted_points) >> 1
        return [
            make_kd_tree(sorted_points[: half], dim, i),
            make_kd_tree(sorted_points[half + 1:], dim, i),
            sorted_points[half]
        ]

    elif len(points) == 1:
        return [None, None, points[0]]

#also optionality to add points to the KD-Tree
def add_point(kd_node, point, dim, i=0):

    if kd_node is not None:
        dx = kd_node[2][i] - point[i]
        i = (i + 1) % dim

        for j, c in ((0, dx >= 0), (1, dx < 0)):

            if c and kd_node[j] is None:
                kd_node[j] = [None, None, point]

            elif c:
                add_point(kd_node[j], point, dim, i)

#create a KD-Tree from California housing dataset
make_kd_tree(data, 8)