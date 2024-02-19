# -*- coding: utf-8 -*-
"""BostonHousingRegression.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1NaLDzVoJFEF6Z-UAnZwAG2ZP9dQl4tZ_
"""

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn import datasets, linear_model
from sklearn.datasets import load_boston
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.model_selection  import train_test_split

"""Load the Boston dataset, turn it into a pandas dataframe, and add the target as 'Price':"""

boston = load_boston()

dataframe = pd.DataFrame(boston['data'])
#dataframe.columns = boston['feature_names']
dataframe['PRICE'] = boston['target']

"""Choose x and y axes, then use sklearn's "train_test_split" method to split them into random train/test subsets:"""

y = dataframe['PRICE']
x = dataframe.iloc[:, 0:13]

X_train, X_test, Y_train, Y_test = train_test_split(x, y, test_size = 0.3, random_state = 0)

"""Create the linear model, then use it to predict future prices using the "predict" method:"""

from sklearn.linear_model import LinearRegression
linear_model = LinearRegression()
linear_model.fit(X_train, Y_train)

dataframe['PRICE_PRED'] = linear_model.predict(dataframe.iloc[:,0:13])

"""Plot the scatter plot:"""

# Commented out IPython magic to ensure Python compatibility.
# %matplotlib inline
plt.scatter(dataframe['PRICE'], dataframe['PRICE_PRED'], s=5 )
plt.xlabel( "Prices")
plt.ylabel( "Predicted Prices")

"""Plot the linear regression line:"""

#Use 'seaborn' regplot method to plot the linear regression
import seaborn
ax = seaborn.regplot(x="PRICE", y="PRICE_PRED", data=dataframe[['PRICE','PRICE_PRED']])