## This file should contain function that feeds data to optimizer

## This file should contain session code which will load model.py and train it using data from data.py
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
rng = np.random
from abc import ABCMeta, abstractmethod # for abstract classes


learning_rate = 0.01
training_epochs = 1000
display_step = 50


class DataFeedBase():
    __metaclass__ = ABCMeta
    
    
    def __init__(self,batch_size):
        self.batch_size = batch_size

    @abstractmethod
    def batches(self):
        pass

    @abstractmethod
    def get_n_samples(self):
        pass
    
class SampleFeed(DataFeedBase):
    
    def __init__(self,*args,**kwargs):
        super(SampleFeed,self).__init__(*args,**kwargs)
        
        sample_size = 28*28

        # X is 3 band normal multivariate with slight correlation between samples
        self.train_X = np.random.multivariate_normal([0,2,3],[[1,-0.9,0.1],
                                                   [-0.9,1,0.3],
                                                   [0.1,0.3,1]],size=sample_size)

        X = self.train_X
        # Now set Y setting explisitly coefficents for CCA to recover and add some multivariate noise
        self.train_Y = np.concatenate( 
            (
            ( X[:,0]*0.3+X[:,1]*-0.2+X[:,2]*0.4 )[:,np.newaxis],
            ( X[:,0]*-0.02+X[:,1]*1.2+X[:,2]*2.4 )[:,np.newaxis],
            ( X[:,0]*0.2+X[:,1]*-0.1+X[:,2]*0.03 )[:,np.newaxis],
            ),
            axis=1
        ) + np.random.normal(0,0.3,size=(sample_size,3)) + 1

    def batches(self):

        # Number of steps is number of batches to be fed
        num_steps = int(self.get_n_samples()/self.batch_size)

        for step in range(num_steps):

            offset = (step * self.batch_size) % (self.get_n_samples() - self.batch_size)

            batch_data = self.train_X[offset:(offset + self.batch_size)]
            batch_labels = self.train_Y[offset:(offset + self.batch_size)]

            yield { 'X' : batch_data, 'Y' : batch_labels }  

    def get_test_data(self):
        return { 'X' : self.train_X, 'Y' : self.train_Y }  

    def get_n_samples(self):
        return self.train_X.shape[0]
    

class DataFeed(DataFeedBase):
    
    def __init__(self,x,y,*args,**kwargs):
        super(DataFeed,self).__init__(*args,**kwargs)
        
        # X is 3 band normal multivariate with slight correlation between samples
        self.train_X = x
        self.train_Y = y

    def batches(self):

        # Number of steps is number of batches to be fed
        num_steps = int(self.get_n_samples()/self.batch_size)

        for step in range(num_steps):

            offset = (step * self.batch_size) % (self.get_n_samples() - self.batch_size)

            batch_data = self.train_X[offset:(offset + self.batch_size)]
            batch_labels = self.train_Y[offset:(offset + self.batch_size)]

            yield { 'X' : batch_data, 'Y' : batch_labels }  

    def get_test_data(self):
        return { 'X' : self.train_X, 'Y' : self.train_Y }  

    def get_n_samples(self):
        return self.train_X.shape[0]

        
        