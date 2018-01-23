#!/usr/bin/python3
"""
Module for calculation of CCA without access to whole data
"""

import numpy as np
from scipy.linalg import sqrtm

# Correlation function
def calc_covariance(xy_sum,x_sum,y_sum,n):
    """
    calculate covariance matrix of 2 variables
    """
    return ( xy_sum - np.matmul(x_sum,y_sum.T)/n )/(n-1)


def calc_linear_regression(n,a,b,x_sum, y_sum, xy_sum, xx_sum ):
    m1 = np.array([[n,(a @ x_sum)[0]],
                  [(a @ x_sum)[0],((a[:,np.newaxis] @ a[:,np.newaxis].T) * xx_sum).sum()]])
    m2 = np.array([
        (b @ y_sum)[0],
        ( ( a[:,np.newaxis] @ b[np.newaxis,:] ) * ( xy_sum ) ).sum()
    ])
    return np.linalg.inv(m1) @ m2

def cca(x,y):
    x_sum = np.sum(x,axis=0)[:,np.newaxis]
    y_sum = np.sum(y,axis=0)[:,np.newaxis]
    xy_sum = np.transpose(x) @ y
    xx_sum = np.transpose(x) @ x
    yy_sum = np.transpose(y) @ y
    n = np.shape(x)[0]

    xy_cov = calc_covariance(xy_sum,x_sum,y_sum,n)
    xx_cov = calc_covariance(xx_sum,x_sum,x_sum,n)
    yy_cov = calc_covariance(yy_sum,y_sum,y_sum,n)


    xx_cov_sqrt_inv = np.linalg.inv( sqrtm(xx_cov) )
    yy_cov_sqrt_inv = np.linalg.inv( sqrtm(yy_cov) )
    
    u_mat = xx_cov_sqrt_inv @ xy_cov @ np.linalg.inv(yy_cov) @ xy_cov.T @ xx_cov_sqrt_inv
    u_eigvals,u_eigvecs = np.linalg.eig(u_mat)

    v_mat = yy_cov_sqrt_inv @ xy_cov.T @ np.linalg.inv(xx_cov) @ xy_cov @ yy_cov_sqrt_inv
    v_eigvals,v_eigvecs = np.linalg.eig(v_mat)

    # Sort eigenvectors by their eigenvalues
    # The hypothisis here is that correlation is bigger if eigenvalue of eigenvector is bigger
    u = u_eigvecs.T[sorted([i for i in range(len(u_eigvals))], key=lambda x: -u_eigvals[x])]
    v = v_eigvecs.T[sorted([i for i in range(len(v_eigvals))], key=lambda x: -v_eigvals[x])]

    a = (u @ xx_cov_sqrt_inv).T
    b = (v @ yy_cov_sqrt_inv).T
    
    regressions = np.array( [ calc_linear_regression(n,a[:,i],b[:,i],x_sum, y_sum, xy_sum, xx_sum ) for i in range(a.shape[1]) ] )
    
    a = a*regressions[:,1]

    return a,b,regressions[:,0]

class CCA():

    def __init__(self,bands_n):
        self.bands_n = bands_n
        self.reset()

    def reset(self):
        self.x_sum = np.zeros(self.bands_n)[:,np.newaxis]
        self.y_sum = np.zeros(self.bands_n)[:,np.newaxis]
        self.xy_sum = np.zeros((self.bands_n,self.bands_n))
        self.xx_sum = np.zeros((self.bands_n,self.bands_n))
        self.yy_sum = np.zeros((self.bands_n,self.bands_n))
        self.a = np.zeros((self.bands_n,self.bands_n))
        self.b = np.zeros((self.bands_n,self.bands_n))
        self.reg = np.zeros((self.bands_n,2))
        self.n = 0
        
        
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
        
    def calc(self):
        
        xx_cov = calc_covariance(self.xx_sum,self.x_sum,self.x_sum,self.n)
        xy_cov = calc_covariance(self.xy_sum,self.x_sum,self.y_sum,self.n)
        yy_cov = calc_covariance(self.yy_sum,self.y_sum,self.y_sum,self.n)
        
        xx_cov_sqrt_inv = np.linalg.inv( sqrtm(xx_cov) )
        yy_cov_sqrt_inv = np.linalg.inv( sqrtm(yy_cov) )
    
        u_mat = xx_cov_sqrt_inv @ xy_cov @ np.linalg.inv(yy_cov) @ xy_cov.T @ xx_cov_sqrt_inv
        u_eigvals,u_eigvecs = np.linalg.eig(u_mat)

        v_mat = yy_cov_sqrt_inv @ xy_cov.T @ np.linalg.inv(xx_cov) @ xy_cov @ yy_cov_sqrt_inv
        v_eigvals,v_eigvecs = np.linalg.eig(v_mat)
        
        # Sort eigenvectors by their eigenvalues
        # The hypothisis here is that correlation is bigger if eigenvalue of eigenvector is bigger
        u = u_eigvecs.T[sorted([i for i in range(len(u_eigvals))], key=lambda x: -u_eigvals[x])]
        v = v_eigvecs.T[sorted([i for i in range(len(v_eigvals))], key=lambda x: -v_eigvals[x])]
        
        self.a = (u @ xx_cov_sqrt_inv).T
        self.b = (v @ yy_cov_sqrt_inv).T
        
        # fix possible result for negarive correlation
        self.reg = np.array( [ calc_linear_regression(self.n,self.a[:,i],self.b[:,i],self.x_sum, self.y_sum, self.xy_sum, self.xx_sum ) for i in range(self.a.shape[1]) ] )
        self.a = self.a*self.reg[:,1]
        
        return self.a,self.b, self.reg
    
    def transform(self,X,Y,apply_intercept=False):
        if apply_intercept:
            u = np.dot( X,self.a )+self.reg[:,0]
        else:
            u = np.dot( X,self.a )
        v = np.dot( Y,self.b )
            
        return u,v