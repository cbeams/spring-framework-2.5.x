package org.springframework.beans.factory.script.groovy

class Test implements Hello {

	property message
 
	String sayHello() {
		message 
	}      
} 