from collections import defaultdict

from matplotlib.pyplot import show, legend, plot, fill_between, grid, xlabel, ylabel, title

import numpy as np

nastyX = None
nastyY = None
nastyCorr = None

# Generate samples
def gen_samples(sample_size,n_bands=3):
    X = np.random.normal(0,1,size=(sample_size,n_bands))
    Y = X+np.random.normal(0,0.5,size=(sample_size,n_bands))
    return X,Y

# Print info about cca
def cca_info(X,Y, A, B):
    
    prev_u = None
    prev_v = None
    
    for i,(a,b) in enumerate( zip(A.T,B.T) ):
        print('{} cannonical variates pair'.format(i+1))
                
        u = np.dot( X,a )
        v = np.dot( Y,b )

        if prev_u is not None and prev_v is not None:
            print('prev corrcoef:\n{: <10.4f}{: <10.4f}\n{: <10.4f}{: <10.4f}'.format( 
                np.corrcoef(u,prev_u)[0,1], np.corrcoef(u,prev_v)[0,1],
                np.corrcoef(v,prev_u)[0,1], np.corrcoef(v,prev_v)[0,1]
            ))
        prev_u = u
        prev_v = v

        print('corrcoef: {:.4f}'.format(np.corrcoef(u,v)[0,1]))

        # Fit u more tightly to v
        reg = LinearRegression()
        reg.fit(u[:,np.newaxis],v)
        new_u = reg.predict(u[:,np.newaxis])

        plot(new_u[:10],'C0-',label='u')
        plot(u[:10],'C0--')
        plot(v[:10],'C1',label='v')
        legend()
        show()
        
def random_subsample(sample_size,*args):
    ret = []
    ids = np.array([i for i in range(len(args[0]))])
    np.random.shuffle(ids)
    
    for arg in args:
        ret.append(np.array(arg)[ids][:sample_size])
        
    return ret

nastyCorr = None
nastyX = None
nastyY = None

# Compare CCA
def cca_compare(X,Y, funcs_dict, x_samples = 20, n_probas = 10,train_eval=False):
    
    func_labels = list(funcs_dict.keys())
    funcs = [funcs_dict[i] for i in func_labels]
    
    sample_sizes = []
    
    correlations_mean = defaultdict(list)
    correlations_max = defaultdict(list)
    correlations_min = defaultdict(list)
    
    for sample_size in np.linspace(int(X.shape[0]*0.2),X.shape[0],x_samples).astype(np.int):
        sample_sizes.append(sample_size)
        
        for func in funcs:

            corr_pack_mean = []
            corr_pack_max = []
            corr_pack_min = []
            
            for n_proba in range(n_probas):
                corr_pack = []
            
                Xs,Ys = random_subsample(sample_size,X,Y)
                A,B = func(Xs,Ys)
            
                for i,(a,b) in enumerate( zip(A.T,B.T) ):
                    if train_eval:
                        u = np.dot( Xs,a )
                        v = np.dot( Ys,b )
                    else:
                        u = np.dot( X,a )
                        v = np.dot( Y,b )
                    corr_pack.append( np.abs( np.corrcoef(u,v)[0,1] ) )

                global nastyCorr
                if np.array(corr_pack).max() > 0 and (nastyCorr is None or nastyCorr > np.array(corr_pack).max() ):
                    global nastyX
                    global nastyY
                    nastyX = Xs
                    nastyY = Ys
                    nastyCorr = np.array(corr_pack).max()
                    #print("nastyCorr: {}".format(nastyCorr))
                    
                corr_pack_mean.append(corr_pack)

            correlations_min[func].append( np.percentile(corr_pack_mean,1,axis=0) )
            correlations_mean[func].append( np.percentile(corr_pack_mean,50,axis=0) )
            correlations_max[func].append( np.percentile(corr_pack_mean,99,axis=0) )
    
    corr_by_func_min = defaultdict(lambda: defaultdict(list))
    corr_by_func_mean = defaultdict(lambda: defaultdict(list))
    corr_by_func_max = defaultdict(lambda: defaultdict(list))
    
    for func_id,func in enumerate(funcs):
        for component_id, component_corr in enumerate( np.array( correlations_min[func] ).T ):
            corr_by_func_min[component_id][func_id] = component_corr
        for component_id, component_corr in enumerate( np.array( correlations_mean[func] ).T ):
            corr_by_func_mean[component_id][func_id] = component_corr
        for component_id, component_corr in enumerate( np.array( correlations_max[func] ).T ):
            corr_by_func_max[component_id][func_id] = component_corr
        
    for component_id in corr_by_func_mean.keys():
        for func_i,(func,label) in enumerate( zip(funcs,func_labels) ):
            
            if func_i not in corr_by_func_mean[component_id]:
                continue
                
            color = 'C{}'.format(func_i)
            
            min_x  = corr_by_func_min[component_id][func_i]
            mean_x = corr_by_func_mean[component_id][func_i]
            max_x  = corr_by_func_max[component_id][func_i]
            
            plot(sample_sizes, mean_x,color,label=label)
            fill_between(sample_sizes,min_x,max_x,facecolor=color,alpha=0.1)
        
        grid(True)
        title('Comparison of CCA. Band {}'.format(component_id))
        xlabel("sub-Sample size")
        ylabel("Correlation")
        legend()
        show()