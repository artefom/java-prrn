#!/usr/bin/python3
"""
Module for calculation of CCA without access to whole data
"""

import numpy as np

class CCA():

	def __init__(self,bands_n):
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

		return a,b

