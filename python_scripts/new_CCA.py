#!/usr/bin/python3
"""
Module for calculation of CCA without access to whole data
"""

import numpy as np
from scipy.linalg import sqrtm

def calc_covariance(xy_wsum,x_wsum,y_wsum,w_sum):
    """
    calculate weighted covariance matrix of 2 variables
    """
    return (xy_wsum - (x_wsum @ y_wsum.T)/w_sum)/(w_sum-1)

def calc_linear_regression( w_sum,a,b,x_wsum, y_wsum, xy_wsum, xx_wsum ):
    m1 = np.array([[w_sum,(a @ x_wsum)[0]],
                  [(a @ x_wsum)[0],((a[:,np.newaxis] @ a[:,np.newaxis].T) * xx_wsum).sum()]])
    m2 = np.array([
        (b @ y_wsum)[0],
        ( ( a[:,np.newaxis] @ b[np.newaxis,:] ) * ( xy_wsum ) ).sum()
        ])
    return np.linalg.inv(m1) @ m2

class CCA():

    def __init__(self,bands_n):
        self.bands_n = bands_n
        self.reset()

    def reset(self):
        
        # sum of weights
        self.w_sum = 0
        
        # weighted statistics
        self.x_wsum  = np.zeros(self.bands_n)[:,np.newaxis]
        self.y_wsum  = np.zeros(self.bands_n)[:,np.newaxis]
        self.xy_wsum = np.zeros((self.bands_n,self.bands_n))
        self.xx_wsum = np.zeros((self.bands_n,self.bands_n))
        self.yy_wsum = np.zeros((self.bands_n,self.bands_n))

        # variables to hold result
        self.a = np.zeros((self.bands_n,self.bands_n))
        self.b = np.zeros((self.bands_n,self.bands_n))
        self.reg = np.zeros((self.bands_n,2))
        
        
    def push(self,x,y,w = None):
        
        if w is not None:
            self.xy_wsum += np.transpose(x) @ ( y*w[:,np.newaxis] )
            self.xx_wsum += np.transpose(x) @ ( x*w[:,np.newaxis] )
            self.yy_wsum += np.transpose(y) @ ( y*w[:,np.newaxis] )
            self.x_wsum  += np.sum(x*w[:,np.newaxis],axis=0)[:,np.newaxis]
            self.y_wsum  += np.sum(y*w[:,np.newaxis],axis=0)[:,np.newaxis]
            self.w_sum += w.sum()
        else:
            # assuming all weights are 1
            self.xy_wsum += np.transpose(x) @ y
            self.xx_wsum += np.transpose(x) @ x
            self.yy_wsum += np.transpose(y) @ y
            self.x_wsum  += np.sum(x,axis=0)[:,np.newaxis]
            self.y_wsum  += np.sum(y,axis=0)[:,np.newaxis]
            self.w_sum += np.shape(x)[0]
            
    def pull(self,x,y):
        if w is not None:
            self.xy_wsum -= np.transpose(x) @ ( y*w[:,np.newaxis] )
            self.xx_wsum -= np.transpose(x) @ ( x*w[:,np.newaxis] )
            self.yy_wsum -= np.transpose(y) @ ( y*w[:,np.newaxis] )
            self.x_wsum  -= np.sum(x*w[:,np.newaxis],axis=0)[:,np.newaxis]
            self.y_wsum  -= np.sum(y*w[:,np.newaxis],axis=0)[:,np.newaxis]
            self.w_sum -= w.sum()
        else:
            # assuming all weights are 1
            self.xy_wsum -= np.transpose(x) @ y
            self.xx_wsum -= np.transpose(x) @ x
            self.yy_wsum -= np.transpose(y) @ y
            self.x_wsum  -= np.sum(x,axis=0)[:,np.newaxis]
            self.y_wsum  -= np.sum(y,axis=0)[:,np.newaxis]
            self.w_sum -= np.shape(x)[0]
            
        
    def calc(self):
        
        #def calc_covariance_weighted(xy_wsum,x_sum,x_wsum,y_sum,y_wsum,n,w_sum):
        
        self.xx_cov = calc_covariance(self.xx_wsum,self.x_wsum,self.x_wsum,self.w_sum)
        self.xy_cov = calc_covariance(self.xy_wsum,self.x_wsum,self.y_wsum,self.w_sum)
        self.yy_cov = calc_covariance(self.yy_wsum,self.y_wsum,self.y_wsum,self.w_sum)
        
        self.xx_cov_sqrt_inv = np.linalg.inv( sqrtm(self.xx_cov) )
        self.yy_cov_sqrt_inv = np.linalg.inv( sqrtm(self.yy_cov) )
    
        u_mat = self.xx_cov_sqrt_inv @ self.xy_cov @ np.linalg.inv(self.yy_cov) @ self.xy_cov.T @ self.xx_cov_sqrt_inv
        self.u_eigvals,u_eigvecs = np.linalg.eig(u_mat)

        v_mat = self.yy_cov_sqrt_inv @ self.xy_cov.T @ np.linalg.inv(self.xx_cov) @ self.xy_cov @ self.yy_cov_sqrt_inv
        self.v_eigvals,v_eigvecs = np.linalg.eig(v_mat)
        
        # Sort eigenvectors by their eigenvalues
        # The hypothisis here is that correlation is bigger if eigenvalue of eigenvector is bigger
        u = u_eigvecs.T[sorted([i for i in range(len(self.u_eigvals))], key=lambda x: -self.u_eigvals[x])]
        v = v_eigvecs.T[sorted([i for i in range(len(self.v_eigvals))], key=lambda x: -self.v_eigvals[x])]
        
        self.a = (u @ self.xx_cov_sqrt_inv).T
        self.b = (v @ self.yy_cov_sqrt_inv).T
                    
        #self.reg = np.array( [ calc_linear_regression(self.n,self.a[:,i],self.b[:,i],self.x_sum, self.y_sum, self.xy_sum, self.xx_sum ) for i in range(self.a.shape[1]) ] )
        
        #fix possible result for negarive correlation    
        self.reg = np.array([
            calc_linear_regression( self.w_sum, self.a[:,i], self.b[:,i], self.x_wsum, self.y_wsum, self.xy_wsum, self.xx_wsum ) for i in range(self.bands_n)
        ])
        
        self.a = self.a*self.reg[:,1]
        
        return self.a,self.b, self.reg
    
    def transform(self,X,Y,apply_intercept=False):
        if apply_intercept:
            u = np.dot( X,self.a )+self.reg[:,0]
        else:
            u = np.dot( X,self.a )
        v = np.dot( Y,self.b )
            
        return u,v