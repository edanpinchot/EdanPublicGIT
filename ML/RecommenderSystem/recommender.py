# -*- coding: utf-8 -*-
"""Recommender.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1VQQVjYwk9ahgKIAne6TV51t7dCpMtjNw
"""

# importing libraries
import pandas as pd
import numpy as np

# movielens data

# reading ratings file:
ratings = pd.read_csv("http://files.grouplens.org/datasets/movielens/ml-100k/u1.base",
                      sep='\t',
                      header=None, 
                      names=["userId", "movieId", "rating", "timestamp"])

# reading users file:
users = pd.read_csv('http://files.grouplens.org/datasets/movielens/ml-100k/u.user', 
                    sep='|', 
                    header=None,
                    names=['user_id', 'age', 'sex', 'occupation', 'zip_code'], encoding='latin-1')

# reading items file:
items = pd.read_csv("http://files.grouplens.org/datasets/movielens/ml-100k/u.item", 
                    sep='|', 
                    header=None, 
                    encoding="latin1",
                    names=["movieId", "title", "date released", "video_release_date", "link", "Genere_1", "Genere_2", "Genere_3", "Genere_4", "Genere_5", "Genre_6", "Genere_7", "Genere_8", "Genre_9", "Genere_10", "Genere_11", "Genere_12", "Genere_13", "Genere_14", "Genre_15", "Genere_16", "Genere_17", "Genre_18", "Genre_19"])

# reading data file:
data = pd.read_csv("http://files.grouplens.org/datasets/movielens/ml-100k/u.data",
                      sep='\t',
                      header=None, 
                      names=["UserId", "MovieId", "Rating", "Timestamp"])

print("\nRatings Data :")
print("shape : ", ratings.shape)
ratings.head()

print("\nUser Data :")
print("shape : ", users.shape)
users.head()

print("\nItem Data :")
print("shape : ", items.shape)
items.head()

#print("\nData Data :")
#print("shape : ", data.shape)
#data.head()

original_items = items
# getting rid of a title 'unknown'
# TODO: Need to search ratings dataframe and remove reference to movieId no. 267
items = items[items.title != 'unknown']

items.shape

n_users = data.UserId.unique().shape[0]
print(n_users)
n_items = data.MovieId.unique().shape[0]
print(n_items)

data_matrix = np.zeros((n_users, n_items))
for line in data.itertuples():
    data_matrix[line[1]-1, line[2]-1] = line[3]
data_matrix

def train_test_split(ratings):
    test = np.zeros(ratings.shape)
    train = ratings.copy()
    for user in range(ratings.shape[0]):
        test_ratings = np.random.choice(ratings[user, :].nonzero()[0], 
                                        size=10, 
                                        replace=False)
        train[user, test_ratings] = 0.
        test[user, test_ratings] = ratings[user, test_ratings]
        
    # Test and training are truly disjoint
    assert(np.all((train * test) == 0)) 
    return train, test

train, test = train_test_split(data_matrix)

from sklearn.metrics.pairwise import pairwise_distances 
user_similarity = pairwise_distances(data_matrix, metric='cosine')
item_similarity = pairwise_distances(data_matrix.T, metric='cosine')

def predict(ratings, similarity, type='user'):
    if type == 'user':
        mean_user_rating = ratings.mean(axis=1)
        #We use np.newaxis so that mean_user_rating has same format as ratings
        ratings_diff = (ratings - mean_user_rating[:, np.newaxis])
        pred = mean_user_rating[:, np.newaxis] + similarity.dot(ratings_diff) / np.array([np.abs(similarity).sum(axis=1)]).T
    elif type == 'item':
        pred = ratings.dot(similarity) / np.array([np.abs(similarity).sum(axis=1)])
    return pred

user_prediction = predict(data_matrix, user_similarity, type='user')
item_prediction = predict(data_matrix, item_similarity, type='item')
user_prediction

from numpy.linalg import solve

class ExplicitMF():
    def __init__(self, 
                 ratings, 
                 n_factors=40, 
                 item_reg=0.0, 
                 user_reg=0.0,
                 verbose=False):
        """
        Train a matrix factorization model to predict empty 
        entries in a matrix. The terminology assumes a 
        ratings matrix which is ~ user x item
        
        Params
        ======
        ratings : (ndarray)
            User x Item matrix with corresponding ratings
        
        n_factors : (int)
            Number of latent factors to use in matrix 
            factorization model
        
        item_reg : (float)
            Regularization term for item latent factors
        
        user_reg : (float)
            Regularization term for user latent factors
        
        verbose : (bool)
            Whether or not to printout training progress
        """
        
        self.ratings = ratings
        self.n_users, self.n_items = ratings.shape
        self.n_factors = n_factors
        self.item_reg = item_reg
        self.user_reg = user_reg
        self._v = verbose

    def als_step(self,
                 latent_vectors,
                 fixed_vecs,
                 ratings,
                 _lambda,
                 type='user'):
        """
        One of the two ALS steps. Solve for the latent vectors
        specified by type.
        """
        if type == 'user':
            # Precompute
            YTY = fixed_vecs.T.dot(fixed_vecs)
            lambdaI = np.eye(YTY.shape[0]) * _lambda

            for u in range(latent_vectors.shape[0]):
                latent_vectors[u, :] = solve((YTY + lambdaI), 
                                             ratings[u, :].dot(fixed_vecs))
        elif type == 'item':
            # Precompute
            XTX = fixed_vecs.T.dot(fixed_vecs)
            lambdaI = np.eye(XTX.shape[0]) * _lambda
            
            for i in range(latent_vectors.shape[0]):
                latent_vectors[i, :] = solve((XTX + lambdaI), 
                                             ratings[:, i].T.dot(fixed_vecs))
        return latent_vectors

    def train(self, n_iter=10):
        """ Train model for n_iter iterations from scratch."""
        # initialize latent vectors
        self.user_vecs = np.random.random((self.n_users, self.n_factors))
        self.item_vecs = np.random.random((self.n_items, self.n_factors))
        
        self.partial_train(n_iter)
    
    def partial_train(self, n_iter):
        """ 
        Train model for n_iter iterations. Can be 
        called multiple times for further training.
        """
        ctr = 1
        while ctr <= n_iter:
            if ctr % 10 == 0 and self._v:
                print("\tcurrent iteration: {}".format(ctr))
            self.user_vecs = self.als_step(self.user_vecs, 
                                           self.item_vecs, 
                                           self.ratings, 
                                           self.user_reg, 
                                           type='user')
            self.item_vecs = self.als_step(self.item_vecs, 
                                           self.user_vecs, 
                                           self.ratings, 
                                           self.item_reg, 
                                           type='item')
            ctr += 1
    
    def predict_all(self):
        """ Predict ratings for every user and item. """
        predictions = np.zeros((self.user_vecs.shape[0], 
                                self.item_vecs.shape[0]))
        for u in range(self.user_vecs.shape[0]):
            for i in range(self.item_vecs.shape[0]):
                predictions[u, i] = self.predict(u, i)
                
        return predictions
    def predict(self, u, i):
        """ Single user and item prediction. """
        return self.user_vecs[u, :].dot(self.item_vecs[i, :].T)
    
    def calculate_learning_curve(self, iter_array, test):
        """
        Keep track of RMSE as a function of training iterations.
        
        Params
        ======
        iter_array : (list)
            List of numbers of iterations to train for each step of 
            the learning curve. e.g. [1, 5, 10, 20]
        test : (2D ndarray)
            Testing dataset (assumed to be user x item).
        
        The function creates two new class attributes:
        
        train_rmse : (list)
            Training data MSE values for each value of iter_array
        test_rmse : (list)
            Test data MSE values for each value of iter_array
        """
        iter_array.sort()
        self.train_rmse =[]
        self.test_rmse = []
        iter_diff = 0
        for (i, n_iter) in enumerate(iter_array):
            if self._v:
                print('Iteration: {}'.format(n_iter))
            if i == 0:
                self.train(n_iter - iter_diff)
            else:
                self.partial_train(n_iter - iter_diff)

            predictions = self.predict_all()

            self.train_rmse += [get_rmse(predictions, self.ratings)]
            self.test_rmse += [get_rmse(predictions, test)]
            if self._v:
                print('Train rmse: ' + str(self.train_rmse[-1]))
                print('Test rmse: ' + str(self.test_rmse[-1]))
            iter_diff = n_iter

#mf = MF(data_matrix, K=20, alpha=0.001, beta=0.01, iterations=100)
#training_process = mf.train()
#print()
#print("P x Q:")
#print(mf.full_matrix())
#print()

from sklearn.metrics import mean_squared_error

def get_rmse(pred, actual):
    # Ignore nonzero terms.
    pred = pred[actual.nonzero()].flatten()
    actual = actual[actual.nonzero()].flatten()
    return mean_squared_error(pred, actual, squared=False)

mf = ExplicitMF(train, n_factors=10, user_reg=0.1, item_reg=0.1)
iter_array = [1, 2, 5, 10, 25, 50, 100]
mf.calculate_learning_curve(iter_array, test)

# Commented out IPython magic to ensure Python compatibility.
# %matplotlib inline
import matplotlib.pyplot as plt
import seaborn as sns
sns.set()

def plot_learning_curve(iter_array, model):
    plt.plot(iter_array, model.train_rmse, \
             label='Training', linewidth=5)
    plt.plot(iter_array, model.test_rmse, \
             label='Test', linewidth=5)


    plt.xticks(fontsize=16);
    plt.yticks(fontsize=16);
    plt.xlabel('iterations', fontsize=30);
    plt.ylabel('RMSE', fontsize=30);
    plt.legend(loc='best', fontsize=20);

plot_learning_curve(iter_array, mf)

"""## Hybrid Model:"""

def __init__(self, FLAGS):    
    self.FLAGS=FLAGS
    self.weight_initializer=model_helper._get_weight_initializer()
    self.bias_initializer=model_helper._get_bias_initializer()
    self.init_parameters()
        

    def init_parameters(self):
        ''' Initializing the weights and biasis of the neural network.'''
        
        with tf.name_scope('weights'):
            self.W_1=tf.get_variable(name='weight_1', shape=(self.FLAGS.num_v,self.FLAGS.num_h), 
                                     initializer=self.weight_initializer)
            self.W_2=tf.get_variable(name='weight_2', shape=(self.FLAGS.num_h,self.FLAGS.num_h), 
                                     initializer=self.weight_initializer)
            self.W_3=tf.get_variable(name='weight_3', shape=(self.FLAGS.num_h,self.FLAGS.num_h), 
                                     initializer=self.weight_initializer)
            self.W_4=tf.get_variable(name='weight_5', shape=(self.FLAGS.num_h,self.FLAGS.num_v), 
                                     initializer=self.weight_initializer)
            
        with tf.name_scope('biases'):
            self.b1=tf.get_variable(name='bias_1', shape=(self.FLAGS.num_h), 
                                    initializer=self.bias_initializer)
            self.b2=tf.get_variable(name='bias_2', shape=(self.FLAGS.num_h), 
                                    initializer=self.bias_initializer)
            self.b3=tf.get_variable(name='bias_3', shape=(self.FLAGS.num_h), 
                                    initializer=self.bias_initializer)

def _inference(self, x):
    '''Making one forward pass. Predicting the outputs, given the inputs.'''
    
    with tf.name_scope('inference'):
         a1=tf.nn.sigmoid(tf.nn.bias_add(tf.matmul(x, self.W_1),self.b1))
         a2=tf.nn.sigmoid(tf.nn.bias_add(tf.matmul(a1, self.W_2),self.b2))
         a3=tf.nn.sigmoid(tf.nn.bias_add(tf.matmul(a2, self.W_3),self.b3))   
         a4=tf.matmul(a3, self.W_4) 
    return a4

def _compute_loss(self, predictions, labels, num_labels):
  ''' Computing the Mean Squared Error loss between the input and output of the network.
	
  @param predictions: predictions of the stacked autoencoder
  @param labels: input values of the stacked autoencoder which serve as labels at the same time
  @param num_labels: number of labels !=0 in the data set to compute the mean
		
  @return mean squared error loss tf-operation
  '''
  with tf.name_scope('loss'):
    loss_op=tf.div(tf.reduce_sum(tf.square(tf.subtract(predictions,labels))),num_labels)
  return loss_op

def _optimizer(self, x):
        '''Optimization of the network parameter through stochastic gradient descent.
            
            @param x: input values for the stacked autoencoder.
            
            @return: tensorflow training operation
            @return: ROOT!! mean squared error
        '''
        
        outputs=self._inference(x)
        mask=tf.where(tf.equal(x,0.0), tf.zeros_like(x), x) # indices of zero values in the training set (no ratings)
        num_train_labels=tf.cast(tf.count_nonzero(mask),dtype=tf.float32) # number of non zero values in the training set
        bool_mask=tf.cast(mask,dtype=tf.bool) # boolean mask
        outputs=tf.where(bool_mask, outputs, tf.zeros_like(outputs)) # set the output values to zero if corresponding input values are zero

        MSE_loss=self._compute_loss(outputs,x,num_train_labels)
        
        if self.FLAGS.l2_reg==True:
            l2_loss = tf.add_n([tf.nn.l2_loss(v) for v in tf.trainable_variables()])
            MSE_loss = MSE_loss +  self.FLAGS.lambda_ * l2_loss
        
        train_op=tf.train.AdamOptimizer(self.FLAGS.learning_rate).minimize(MSE_loss)
        RMSE_loss=tf.sqrt(MSE_loss)

        return train_op, RMSE_loss

def _validation_loss(self, x_train, x_test):
        ''' Computing the loss during the validation time.
        @param x_train: training data samples
        @param x_test: test data samples
        @return networks predictions
        @return root mean squared error loss between the predicted and actual ratings
        '''
        outputs=self._inference(x_train) # use training sample to make prediction
        mask=tf.where(tf.equal(x_test,0.0), tf.zeros_like(x_test), x_test) # identify the zero values in the test ste
        num_test_labels=tf.cast(tf.count_nonzero(mask),dtype=tf.float32) # count the number of non zero values
        bool_mask=tf.cast(mask,dtype=tf.bool) 
        outputs=tf.where(bool_mask, outputs, tf.zeros_like(outputs))

        MSE_loss=self._compute_loss(outputs, x_test, num_test_labels)
        RMSE_loss=tf.sqrt(MSE_loss)
        
        return outputs, RMSE_loss

#dae = DAE(self.FLAGS)

