## This file should contain definition of a model

import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt

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

class CannonicalCorrelation(ModelBase):
    
    def __init__(self, batch_size, band_num, *args, **kwargs):
        
        self.band_num = band_num
        self.batch_size = batch_size
        
        self.X = tf.placeholder( tf.float32, shape=(None,band_num) )
        self.Y = tf.placeholder( tf.float32, shape=(None,band_num) )

        x_mean = tf.reduce_mean(self.X, 0)
        y_mean = tf.reduce_mean(self.Y, 0)

        self.a = tf.Variable( [ [ np.random.normal(0,1) ] for i in range(band_num)], dtype=tf.float32, name = 'x_weights_' )
        self.b = tf.Variable( [ [ np.random.normal(0,1) ] for i in range(band_num)], dtype=tf.float32, name = 'y_weights_' )

        self.xx_cov = tf.matmul( tf.transpose(self.X-x_mean), (self.X-x_mean) )/batch_size
        self.yy_cov = tf.matmul( tf.transpose(self.Y-y_mean), (self.Y-y_mean) )/batch_size
        self.xy_cov = tf.matmul( tf.transpose(self.X-x_mean), (self.Y-y_mean) )/batch_size

        
        norm1 = tf.sqrt( tf.matmul( tf.matmul(tf.transpose(self.a),self.xx_cov), self.a ) )
        norm2 = tf.sqrt( tf.matmul( tf.matmul(tf.transpose(self.b),self.yy_cov), self.b ) )

        self.cost = -(tf.matmul( tf.matmul(tf.transpose( self.a ), self.xy_cov), self.b )/norm1/norm2)[0,0]

    def get_cost(self):
        return self.cost
