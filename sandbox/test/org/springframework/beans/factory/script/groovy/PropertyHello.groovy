package org.springframework.beans.factory.script.groovy

class Test implements org.springframework.beans.factory.script.Hello {

	property message
 
	String sayHello() {
		message 
	}      
} 