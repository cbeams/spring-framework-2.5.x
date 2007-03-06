package org.springframework.scripting.groovy;

import org.springframework.scripting.CallCounter;

class GroovyCallCounter implements CallCounter {

	int count;

	void before() {
	  count++;
	}

	int getCalls() {
	  return count;
	}
}
