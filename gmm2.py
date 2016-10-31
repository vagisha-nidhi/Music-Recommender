import numpy as np
from sklearn import mixture
s = [[0.0]*2 for _ in range(8)]
s[0][0] = 5.0
s[1][0] = 4.0
s[2][0] = 5.5
s[3][0] = 18
s[4][0] = 18.5
s[5][0] = 19.5
s[6][0] = 4.5
s[7][0] = 20.0

s = np.asarray(s)
g = mixture.GMM(n_components=3)
g.fit(s) 


print np.asarray(s)
print np.round(g.weights_, 2)
print np.round(g.means_, 2)
print np.round(g.covars_, 2) 
p = [[0.0]*2 for _ in range(2)]
p[0][0] = 3.0
p[1][0] = 22.0

print g.predict_proba(np.asarray(p)) 