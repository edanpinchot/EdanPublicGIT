# -*- coding: utf-8 -*-
"""RestaurantKNNRegressor.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1es3wyqz6ghXNWxmMPuHqVEDRtEBYkTDn
"""

from sklearn.neighbors import NearestNeighbors
from sklearn.neighbors import KNeighborsRegressor
import numpy as np
import pandas as pd

from google.colab import files
uploaded = files.upload()

import io
train = pd.read_csv(io.BytesIO(uploaded['train.csv']))

from sklearn.model_selection import train_test_split
trainingDataX = pd.DataFrame(train)
trainingDataY = trainingDataX.pop('revenue')
train_x, test_x, train_y, test_y = train_test_split(trainingDataX, trainingDataY, test_size=0.3, random_state=1)

train_x[["day", "month", "year"]] = train_x["Open Date"].str.split("/", expand = True)
train_x.pop("Open Date")
test_x[["day", "month", "year"]] = test_x["Open Date"].str.split("/", expand = True)
test_x.pop("Open Date")

from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import OneHotEncoder
label_encoder = LabelEncoder()

cityGroupEncoded = label_encoder.fit_transform(train_x["City Group"])
typeEncoded = label_encoder.fit_transform(train_x["Type"])
cityEncoded = label_encoder.fit_transform(train_x["City"])
train_x["City Group"] = cityGroupEncoded
train_x["Type"] = typeEncoded
train_x["City"] = cityEncoded

cityGroupEncoded = label_encoder.fit_transform(test_x["City Group"])
typeEncoded = label_encoder.fit_transform(test_x["Type"])
cityEncoded = label_encoder.fit_transform(test_x["City"])
test_x["City Group"] = cityGroupEncoded
test_x["Type"] = typeEncoded
test_x["City"] = cityEncoded

knn = KNeighborsRegressor(n_neighbors = 3)
knn.fit(train_x, train_y)

pred = knn.predict(test_x)
knn.score(test_x, test_y)
#np.column_stack((test_y, pred))

from sklearn.metrics import mean_absolute_error
from sklearn.metrics import mean_squared_error
print("MAE =", mean_absolute_error(test_y, pred))
print("MSE =", mean_squared_error(test_y, pred, squared=True))
print("RMSE =", mean_squared_error(test_y, pred, squared=False))

