# -*- coding: utf-8 -*-
"""IrisL2Regularization.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1-81BEp5pAaRx4OOfugqr4j4u_srdS96O
"""

#imports
from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder

iris = load_iris()
features = iris.data
irisType = iris.target
irisType2 = iris.target.reshape(-1, 1)

petalLength = [row[2] for row in features]
petalWidth = [row[3] for row in features]

# One Hot encode the class labels
encoder = OneHotEncoder(sparse = False)
y = encoder.fit_transform(irisType2)

import numpy as np
x = np.column_stack((petalWidth, petalLength))

#split the data into 60% training data and 30% test data
x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.30)

#create first model that classifies Setosa Irises
model = Sequential()
model.add(Dense(10, input_shape=(2,), activation='relu', name='fc1'))
model.add(Dense(10, activation='relu', name='fc2'))
model.add(Dense(3, activation='softmax', name='output'))
optimizer = Adam(lr=0.1)
model.compile(optimizer, loss='categorical_crossentropy', metrics=['accuracy'])

model.fit(x_train, y_train, epochs=100)

model.evaluate(x_test, y_test)

import tensorflow as tf
from tensorflow import keras

#create second model, this time using L2 regularization
model2 = Sequential()
model2.add(Dense(10, input_shape=(2,), activation='relu', name='fc1'))
model2.add(Dense(10, activation='relu', name='fc2', kernel_regularizer=keras.regularizers.l2(0.1)))
model2.add(Dense(3, activation='softmax', name='output'))
optimizer = Adam(lr=0.001)
model2.compile(optimizer, loss='categorical_crossentropy', metrics=['accuracy'])

model2.fit(x_train, y_train, epochs=100)

model2.evaluate(x_test, y_test)