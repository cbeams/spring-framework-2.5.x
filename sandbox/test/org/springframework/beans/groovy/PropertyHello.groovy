package org.springframework.beans.groovy

class Test implements Hello {

	property message

	String sayHello() {
		message
	}
}