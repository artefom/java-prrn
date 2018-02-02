import numpy as np
from scipy import stats
#from . import new_CCA

class IRMAD():
    
    def __init__(self, cca):
        self.cca = cca
        self.correlation_change = []
        self.iteration_num = -1
        self.old_x_weights = None
        self.old_y_weights = None

    def commit_iteration(self):
        self.cca.calc()
        self.u_var  = np.diagonal( self.cca.a.T @ self.cca.xx_cov @ self.cca.a )
        self.v_var  = np.diagonal( self.cca.b.T @ self.cca.yy_cov @ self.cca.b )
        self.uv_cov = np.diagonal( self.cca.a.T @ self.cca.xy_cov @ self.cca.b )
        self.m_var = (self.u_var + self.v_var - 2*self.uv_cov)
        self.u_mean = np.ravel( self.cca.a.T @ self.cca.x_sum/self.cca.n )
        self.v_mean = np.ravel( self.cca.b.T @ self.cca.y_sum/self.cca.n )
        self.m_mean = self.u_mean-self.v_mean
        self.iteration_num += 1
        
        self.a = self.cca.a
        self.b = self.cca.b
        
        self.cca.reset()

    def push_data(self,X,Y):
        self.cca.push(X,Y)
        
    def calc_nochange_proba(self,X,Y):
        
        if self.iteration_num < 0:
            return None
        
        u = np.dot( X, self.a )
        v = np.dot( Y, self.b )
        m = ( (u-v)-self.m_mean )/np.sqrt(self.m_var)
        P = 1-stats.chi2.cdf( (m**2).sum(axis=1), self.cca.bands_n )
        
        return P