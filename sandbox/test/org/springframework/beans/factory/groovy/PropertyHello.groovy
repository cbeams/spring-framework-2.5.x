 package org.springframework.beans.factory.groovy

class Test implements Hello {

	property message
 
	String sayHello() {
		message 
	}      
} 