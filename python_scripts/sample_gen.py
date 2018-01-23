import numpy as np
from scipy.stats import multivariate_normal
from math import radians

def random_orthonormal_basis(n_bands=2):
    vecs = []
    for i in range(n_bands):
        vecs.append(np.random.multivariate_normal(np.zeros(n_bands),np.identity(n_bands)))
    vecs = np.array(vecs)
    Q, R = np.linalg.qr(vecs)
    for i in range(Q.shape[1]):
        if np.random.normal(0,1) < 0.5:
            Q[i] = Q[i]*-1
    return Q

def rotate(vec, rad):
    rotation_matrix = np.identity(vec.shape[0])
    rotation_matrix[0,0] = np.cos(rad)
    rotation_matrix[0,1] = -np.sin(rad)
    rotation_matrix[1,0] = np.sin(rad)
    rotation_matrix[1,1] = np.cos(rad)
    return rotation_matrix @ vec

def proj_fact(a,b):
    return np.dot(b,a)/(np.linalg.norm(a)**2)

def proj(basis,vecs):

    input_vec = False
    if len(vecs.shape) == 1:
        vecs = np.array([vecs])
        input_vec = True
        
    ret = np.zeros( (vecs.shape[0],basis.shape[0]) )
    
    for vec_i,vec in enumerate( vecs ):
        for basis_i,b in enumerate( basis ):
            ret[vec_i][basis_i] = proj_fact(b,vec)
    
    if input_vec:
        return ret[0]
    return ret

def random_rotation_matrix(n_bands):
    angle = radians(np.random.uniform(-360,360))
    basis = random_orthonormal_basis(n_bands)

    ret = []
    for v in np.identity(n_bands):
        vproj = proj(basis,v)
        vproj = rotate(vproj,angle)
        v2 = proj( proj(basis,np.identity(basis.shape[0])), vproj )
        ret.append(v2)
    return np.array(ret)

def random_scaling_matrix(n_bands,std=0.2):
    return np.diag(np.random.normal(size=n_bands,loc=1,scale=std))

def random_covariance_matrix(n_bands,scale_std=0.2):
    R = random_rotation_matrix( n_bands )
    S = random_scaling_matrix( n_bands, scale_std )
    return R @ S @ S @ np.linalg.inv(R)

def random_multivariate_parameters( n_bands, mean_std_scale=1, mean_std=0.2, std_scale_std=0.2, std=0.2, std_scale=1):
    mean = np.random.multivariate_normal( np.zeros(n_bands), random_covariance_matrix(n_bands,mean_std)*mean_std_scale )
    cov = random_covariance_matrix(n_bands,std)*std_scale
    return mean,cov

class ComplexDistribution:
    
    def __init__(self,weights,distrs):
        self.weights = np.array( weights )
        self.distrs = distrs
        
    def rvs(self,n_samples):
        
        if n_samples <= 0:
            return None

        n_samples_by_distr = np.array( self.weights/np.linalg.norm(self.weights)*n_samples, dtype=np.int )
        n_samples_by_distr[n_samples_by_distr<=0] = 0
        
        while n_samples_by_distr.sum() < n_samples:
            n_samples_by_distr[ round(np.random.uniform(0,len(n_samples_by_distr)-1)) ] += 1

        while n_samples_by_distr.sum() > n_samples:
            i = round(np.random.uniform(0,len(n_samples_by_distr)-1))
            while n_samples_by_distr[i] <= 0:
                i = round(np.random.uniform(0,len(n_samples_by_distr)-1))
            n_samples_by_distr[i] -= 1
        
        ret = []
        for d,n in zip(self.distrs,n_samples_by_distr):
            if n == 0:
                continue
            if n == 1:
                ap = np.array([d.rvs(n)])
            else:
                ap = d.rvs(n)
            ret.append(ap)
        ret = np.concatenate(ret)

        np.random.shuffle(ret)
        return ret
        
def create_distribution(*args,mean_bais_scale=1000, **kwargs):
    n_distr = int(np.random.uniform(10,50))
    mean_bais = np.random.normal(size=args[0],loc=0,scale=mean_bais_scale)
    distrs = []
    weights = []
    for i in range(n_distr):
        mean,cov = random_multivariate_parameters(*args,**kwargs)
        mean+=mean_bais
        distrs.append(multivariate_normal(mean,cov))
        weights.append(np.random.normal(1,0.1))
    return ComplexDistribution(weights,distrs)