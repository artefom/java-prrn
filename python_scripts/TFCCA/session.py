## This file should contain session code which will load model.py and train it using data from data.py
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
rng = np.random
from abc import ABCMeta, abstractmethod # for abstract classes

class SessionBase():
    __metaclass__ = ABCMeta
    
    
    def __init__(self,learning_rate=0.001,training_epochs=100,stop_epsilon=0.001,verbose=False,display_step=50):
        self.learning_rate = learning_rate
        self.training_epochs = training_epochs
        self.display_step = display_step
        self.stop_epsilon = stop_epsilon
        self.verbose=verbose

    @abstractmethod
    def optimize(self):
        pass

    @staticmethod
    def process_dict(str_dict,model):
        '''
        Converts {string: data} dict into {placeholder: data} dict
        by model.getattr(string): data
        '''
        feed_dict = {}
        for k,v in str_dict.items():
            feed_dict[model.__getattribute__(k)] = v
        return feed_dict

class AdamOptimizer(SessionBase):
    
    def __init__(self,model,data,*args,**kwargs):
        super().__init__(*args,**kwargs)
        
        self.model = model
        self.data = data
        
        #  Note, minimize() knows to modify W and b because Variable objects are trainable=True by default
        self.optimizer = tf.train.AdamOptimizer(self.learning_rate)\
                        .minimize( self.model.get_cost() )

        # Initialize the variables (i.e. assign their default value)
        self.init = tf.global_variables_initializer()

        self.sess = None

    def optimize(self):

        if self.sess is None:
            self.sess = tf.Session()


        # Run the initializer
        self.sess.run(self.init)
        
        if self.verbose:
            print('Initialization weights:')
            print('x_rotations:', self.sess.run(self.model.a) )
            print('y_rotations:', self.sess.run(self.model.b) )
            print()

          # Fit all training data
        prev_score=float('inf')
        for epoch in range(self.training_epochs):
            
            for str_feed_dict in self.data.batches():

                feed_dict = SessionBase.process_dict(str_feed_dict,self.model)
                # Now we need to convert {string: data} dictionary
                # into {placeholder: data} dictionary

                self.sess.run(self.optimizer, feed_dict=feed_dict )

            if (epoch+1) % 5 == 0:
                feed_dict = SessionBase.process_dict(self.data.get_test_data(),self.model)
                score = -self.sess.run( self.model.get_cost(), feed_dict=feed_dict )
                if abs(score-prev_score) < self.stop_epsilon:
                    break
                prev_score = score

            if self.verbose:
                if (epoch+1) % self.display_step == 0:
                    print('############# Epoch: {: >4} #############'.format(epoch+1) )
                    #print('Costs: ',costs)
                    feed_dict = SessionBase.process_dict(self.data.get_test_data(),self.model)
                    c = self.sess.run( self.model.get_cost(), feed_dict=feed_dict )
                    print('x_rotations:', self.sess.run(self.model.a) )
                    print('y_rotations:', self.sess.run(self.model.b) )
                    print('Corr coeff: ',-c)
                    
        return np.array( self.sess.run(self.model.a) ), np.array( self.sess.run(self.model.b) )
            
    def predict(self, X):
        feed_dict = {self.model.X: X}
        return self.sess.run(self.model.pred, feed_dict=feed_dict)
    
    
