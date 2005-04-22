package org.springframework.binding.support;

import org.springframework.binding.AttributeSource;

public class EmptyAttributeSource implements AttributeSource {

	public static final AttributeSource INSTANCE = new EmptyAttributeSource(); 
	
	private EmptyAttributeSource() {
		
	}
	
	public boolean containsAttribute(String attributeName) {
		return false;
	}

	public Object getAttribute(String attributeName) {
		return null;
	}
}
