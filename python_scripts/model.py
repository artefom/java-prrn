## This file should contain definition of a model

import tensorflow as tf
import numpy
import matplotlib.pyplot as plt
rng = numpy.random
from abc import ABCMeta, abstractmethod # for abstract classes


class ModelBase():
    __metaclass__ = ABCMeta
    
    def __init__(self):
        pass
    
    @abstractmethod
    def get_cost(self):
        return None

    def get_placeholders(self):
        placeholders = dict()

        for k,v in self.__dict__.items():
            if isinstance(v,tf.Tensor):
                if 'placeholder' in v.name.lower():
                    placeholders[k] = v

        return placeholders

class LinearRegression(ModelBase):
    
    def __init__(self):
        
        # Init placeholders for data to be fed into
        self.X = tf.placeholder("float")
        self.Y = tf.placeholder("float")

        self.W = tf.Variable(rng.randn(), name="weight", trainable=True)
        self.b = tf.Variable(rng.randn(), name="bias", trainable=True)

        self.pred = tf.add(tf.multiply(self.X, self.W), self.b)

        self.cost = tf.sqrt(tf.reduce_mean(tf.square(tf.subtract(self.Y, self.pred))))

    def get_cost(self):
        return self.cost
