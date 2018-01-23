#!/usr/bin/python3
"""
Module for calculation of CCA without access to whole data
"""

import numpy as np
from scipy.optimize import minimize

class CCA():

    def __init__(self,bands_n):
        self.n_bands = bands_n
        self.x_sum = np.zeros(bands_n)[:,np.newaxis]
        self.y_sum = np.zeros(bands_n)[:,np.newaxis]
        self.xy_sum = np.zeros((bands_n,bands_n))
        self.xx_sum = np.zeros((bands_n,bands_n))
        self.yy_sum = np.zeros((bands_n,bands_n))
        self.n = 0


    @staticmethod
    def calc_covariance(xy_sum,x_sum,y_sum,n):
        """
        calculate covariance matrix of 2 variables
        """
        return ( xy_sum - np.matmul(x_sum,y_sum.T)/n )/(n-1)


    def push(self,x,y):
        """
        push x and y to caclulate cca. x and y values are not stored
        """
        self.x_sum += np.sum(x,axis=0)[:,np.newaxis]
        self.y_sum += np.sum(y,axis=0)[:,np.newaxis]
        self.xy_sum += np.matmul(np.transpose(x),y)
        self.xx_sum += np.matmul(np.transpose(x),x)
        self.yy_sum += np.matmul(np.transpose(y),y)
        self.n += np.shape(x)[0]

    def pull(self,x,y):
        """
        pull x and y from caclulating cca. x and y values are not stored
        """
        self.x_sum -= np.sum(x,axis=0)[:,np.newaxis]
        self.y_sum -= np.sum(y,axis=0)[:,np.newaxis]
        self.xy_sum -= np.matmul(np.transpose(x),y)
        self.xx_sum -= np.matmul(np.transpose(x),x)
        self.yy_sum -= np.matmul(np.transpose(y),y)
        self.n -= np.shape(x)[0]

    def calc_correlation(self):

        xx_cov = self.calc_xx_cov()
        xy_cov = self.calc_xy_cov()
        yy_cov = self.calc_yy_cov()

        a = np.linalg.eigvals( np.matmul( np.matmul( np.matmul( np.linalg.inv(xx_cov), xy_cov ), np.linalg.inv(yy_cov)), xy_cov.T ) )[:,np.newaxis]
        a = a/np.linalg.norm(a)
        b = np.matmul(np.matmul( np.linalg.inv(yy_cov), xy_cov.T ),a )

        numerator = np.matmul( np.matmul(a.T,xy_cov), b )
        std1 = np.sqrt(  np.matmul( np.matmul(a.T,xx_cov), a) )
        std2 = np.sqrt(  np.matmul( np.matmul(b.T,yy_cov), b) )

        return np.ravel(numerator/std1/std2)[0]


    def calc_xy_cov(self):
        return CCA.calc_covariance(self.xy_sum,self.x_sum,self.y_sum,self.n)

    def calc_xx_cov(self):
        return CCA.calc_covariance(self.xx_sum,self.x_sum,self.x_sum,self.n)

    def calc_yy_cov(self):
        return CCA.calc_covariance(self.yy_sum,self.y_sum,self.y_sum,self.n)

    def calc_ab(self):
        
        xx_cov = self.calc_xx_cov()
        xy_cov = self.calc_xy_cov()
        yy_cov = self.calc_yy_cov()

        a = np.linalg.eigvals( np.matmul( np.matmul( np.matmul( np.linalg.inv(xx_cov), xy_cov ), np.linalg.inv(yy_cov)), xy_cov.T ) )[:,np.newaxis]
        b = np.matmul(np.matmul( np.linalg.inv(yy_cov), xy_cov.T ),a )

        return np.concatenate( (a,b) )

    def correlation_grad_a(self,a,b):
        xx_cov = self.calc_xx_cov()
        xy_cov = self.calc_xy_cov()
        yy_cov = self.calc_yy_cov()
        numerator = np.matmul( np.matmul(a.T,xy_cov), b )
        numerator_derivative = np.matmul(xy_cov, b )
        denominator = np.sqrt( np.matmul(np.matmul( a.T, xx_cov ),a) )
        denominator_derivative = np.matmul(xx_cov,a)/denominator
        multiplier = 1/np.sqrt( np.matmul(np.matmul( b.T, yy_cov ),b) )
        return multiplier*(denominator*numerator_derivative - denominator_derivative*numerator)/denominator**2

    def correlation_grad_b(self,a,b):
        xx_cov = self.calc_xx_cov()
        xy_cov = self.calc_xy_cov()
        yy_cov = self.calc_yy_cov()
        numerator = np.matmul( np.matmul(a.T,xy_cov), b )
        numerator_derivative = np.matmul(xy_cov.T, a )
        denominator = np.sqrt( np.matmul(np.matmul( b.T, yy_cov ),b) )
        denominator_derivative = np.matmul(yy_cov,b)/denominator
        multiplier = np.sqrt( np.matmul(np.matmul( a.T, xx_cov ),a) )
        return multiplier*(denominator*numerator_derivative - denominator_derivative*numerator)/denominator**2
        
    def correlation(self,ab):
        n_bands = int(ab.size/2)
        a = ab[:n_bands]
        b = ab[n_bands:]
        xx_cov = self.calc_xx_cov()
        xy_cov = self.calc_xy_cov()
        yy_cov = self.calc_yy_cov()
        numerator = np.matmul( np.matmul(a.T,xy_cov), b )
        denominator = np.sqrt( np.matmul(np.matmul( a.T, xx_cov ),a) )*np.sqrt( np.matmul(np.matmul( b.T, yy_cov ),b) )
        return numerator/denominator

    def correlation_grad(self,ab):
        n_bands = int(ab.size/2)
        a = ab[:n_bands]
        b = ab[n_bands:]
        a_grad = self.correlation_grad_a(a,b)
        b_grad = self.correlation_grad_b(a,b)
        return np.concatenate((a_grad,b_grad))

    def calc_ab_adv(self,dont_use_ab=False):


        def cor_func(ab):
            return -self.correlation(ab)

        def cor_func_grad(ab):
            return -self.correlation_grad(ab)


        if dont_use_ab:
            starting_points = [np.ones(self.n_bands*2),-np.ones(self.n_bands*2)]
        else:
            default_ab = self.calc_ab()
            starting_points = [default_ab]

        max_fun = float('-inf')
        max_x = None

        # find best solution for starting from multiple points
        for x0 in starting_points:
            opt_res = minimize(cor_func, x0, method = 'SLSQP', jac=cor_func_grad)
            if -opt_res['fun'] > max_fun:
                max_fun = -opt_res['fun']
                max_x = -opt_res['x']

        return max_x
