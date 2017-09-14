def calc_cca(x_mat,y_mat):
    
    clf = Ridge(fit_intercept=False,alpha=0)
    
    loss_max = 0
    loss_max_X1 = None
    loss_max_Y1 = None
    
    for j in range(100):
        G = np.random.normal(0,1,size=(2,2))

        kcca = 2

        X0 = np.matmul(x_mat,G)
        X0_ = np.linalg.qr(X0)[0] ## Apply QR-decomposition

        for t in range(10):
            clf.fit(X0_,y_mat)
            Y1 = np.matmul(X0_,clf.coef_)
            Y1_ = np.linalg.qr(Y1)[0]

            clf.fit(Y1_,x_mat)

            X1 = np.matmul(Y1_,clf.coef_)
            X1_ = np.linalg.qr(X1)[0]

            X0_ = X1_
        
        loss1 = np.corrcoef( X1_[:,0], Y1_[:,0] )[0,1]
        loss2 = np.corrcoef( X1_[:,1], Y1_[:,1] )[0,1]
        
        new_loss = abs(loss1)+abs(loss2)*0.1
        if new_loss > loss_max:
            loss_max_X1 = X1.copy()
            loss_max_Y1 = Y1.copy()
            loss_max = new_loss
        
    return loss_max_X1[:,0],loss_max_Y1[:,0]