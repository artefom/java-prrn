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
        
        self.train_X = np.asarray([3.3,4.4,5.5,6.71,6.93,4.168,9.779,6.182,7.59,2.167,
                                 7.042,10.791,5.313,7.997,5.654,9.27,3.1])
        
        self.train_Y = np.asarray([1.7,2.76,2.09,3.19,1.694,1.573,3.366,2.596,2.53,1.221,
                                 2.827,3.465,1.65,2.904,2.42,2.94,1.3])

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
        
        