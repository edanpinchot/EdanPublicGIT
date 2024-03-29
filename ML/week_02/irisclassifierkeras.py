# -*- coding: utf-8 -*-
"""IrisClassifierKeras.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1wTCiimDP1Ee_hOWHoxFj0uL5j15otH-v
"""

from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder

from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam

iris = load_iris()

features = iris.data
irisType = iris.target
irisType2 = iris.target.reshape(-1, 1)

petalLength = [row[2] for row in features]
petalWidth = [row[3] for row in features]

# Commented out IPython magic to ensure Python compatibility.
import matplotlib.pyplot as plt
import pandas as pd
# %matplotlib inline

df = pd.DataFrame(dict(petalLength=petalLength, petalWidth=petalWidth, irisType=irisType))

fig, ax = plt.subplots()
colors = {0:'red', 1:'purple', 2:'blue'}
ax.scatter(df['petalLength'], df['petalWidth'], c=df['irisType'].apply(lambda x: colors[x]))
plt.title("Petal Length vs. Petal Width")
plt.show

# One Hot encode the class labels
encoder = OneHotEncoder(sparse = False)
irisType3 = encoder.fit_transform(irisType2)

import numpy as np
petals = np.column_stack((petalWidth, petalLength))

train_x, test_x, train_y, test_y = train_test_split(petals, irisType3, test_size=0.20)
                                                    #features

# Build the model

model = Sequential()

model.add(Dense(10, input_shape=(2,), activation='relu', name='fc1'))
model.add(Dense(10, activation='relu', name='fc2'))
model.add(Dense(3, activation='softmax', name='output'))

# Adam optimizer with learning rate of 0.001
optimizer = Adam(lr=0.001)
model.compile(optimizer, loss='categorical_crossentropy', metrics=['accuracy'])

print('Neural Network Model Summary: ')
print(model.summary())

# Train the model
model.fit(train_x, train_y, verbose=2, batch_size=5, epochs=200)

# Test on unseen data

results = model.evaluate(test_x, test_y)

print('Final test set loss: {:4f}'.format(results[0]))
print('Final test set accuracy: {:4f}'.format(results[1]))