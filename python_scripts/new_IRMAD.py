import numpy as np
from scipy import stats
import new_CCA

from sklearn.linear_model import HuberRegressor
from sklearn.linear_model import LinearRegression

class IRMAD():
    
    def __init__(self, n_bands):
        self.n_bands = n_bands
        self.cca = new_CCA.CCA(n_bands)
        self.correlation_change = []
        self.iteration_num = 0

        self.cor_log = []
        
        
    def commit_iteration(self):
        self.cca.calc()
        self.u_var  = np.diagonal( self.cca.a.T @ self.cca.xx_cov @ self.cca.a )
        self.v_var  = np.diagonal( self.cca.b.T @ self.cca.yy_cov @ self.cca.b )
        self.uv_cov = np.diagonal( self.cca.a.T @ self.cca.xy_cov @ self.cca.b )

        uv_cor = self.uv_cov/np.sqrt(self.u_var)/np.sqrt(self.v_var)
        self.cor_log.append(uv_cor)
        
        cor_change = None
        #calc correlation change
        if len(self.cor_log) > 1:
            uv_cor_prev = self.cor_log[-2]
            cor_change = np.sqrt( ((uv_cor-uv_cor_prev)**2).sum() )
        
        self.m_var = (self.u_var + self.v_var - 2*self.uv_cov)
        self.u_mean = np.ravel( self.cca.a.T @ self.cca.x_wsum/self.cca.w_sum )
        self.v_mean = np.ravel( self.cca.b.T @ self.cca.y_wsum/self.cca.w_sum )
        self.m_mean = self.u_mean-self.v_mean
        self.iteration_num += 1
        
        self.a = self.cca.a
        self.b = self.cca.b
        
        self.cca.reset()
        
        # return corelation change
        return cor_change

    def push_data(self,X,Y):
        self.cca.push(X,Y,w=self.calc_nochange_proba(X,Y))
        
    def calc_nochange_proba(self,X,Y):
        
        if self.iteration_num <= 0:
            return np.ones(X.shape[:-1])
        
        U = self.a.T @ X.T
        V = self.b.T @ Y.T
    
        M = ( ( (U-V).T-self.m_mean)/np.sqrt(self.m_var) ).T
        W = ( 1-stats.chi2.cdf( (M**2).sum(axis=0) , self.n_bands, 0 ) )
    
        return W
    
    def calc_transformation(self,X,Y,fit_algo="OLS",weight_cutoff=0):
        
        if fit_algo == "Huber":
            reg = HuberRegressor()
        elif fit_algo == "OLS":
            reg = LinearRegression()
        else:
            raise ValueError("Unknown fit algorithm: {}".format(fit_algo))
            
        w = self.calc_nochange_proba(X,Y)
    
        # calc cutoff
        x_reg = X[ w > weight_cutoff ]
        y_reg = Y[ w > weight_cutoff ]
        w_reg = w[ w > weight_cutoff ]
        
        ret = []
        
        for i in range( x_reg.shape[1] ):
            reg.fit(y_reg[:,i,np.newaxis],x_reg[:,i],w_reg)
            ret.append( [reg.coef_[0],reg.intercept_ ] )
            
        return np.array(ret)
    
    def apply_transformation(self,X,Y,trans):
        
        last_band = Y.shape
        
        ret_Y = Y.copy()
        
        for band in range(Y.shape[-1]):
            ret_Y[...,band] = ret_Y[...,band]*trans[band][0]+trans[band][1] # multiply band by coef and add intercept
            
        return ret_Y
    
    def get_stats(self,X,Y):
        U = self.a.T @ X.T
        V = self.b.T @ Y.T
        
        return U,V