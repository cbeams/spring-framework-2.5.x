package org.springframework.web.flow.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter class to set data in a map using the <code>AttributeSetter</code>
 * interface.
 */
public class MapAttributeSetterAdapter extends AttributeSetterSupport {

	private Map map;

	public MapAttributeSetterAdapter() {

	}

	/**
	 * Create a new map attribute setter adapter.
	 * @param map the map to wrap
	 */
	public MapAttributeSetterAdapter(Map map) {
		this.map = map;
	}

	public Map getAttributeMap() {
		if (map != null) {
			return map;
		}
		else {
			return Collections.EMPTY_MAP;
		}
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		if (map == null) {
			map = new HashMap();
		}
		map.put(attributeName, attributeValue);
	}

	public boolean containsAttribute(String attributeName) {
		if (map == null) {
			return false;
		}
		return map.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		if (map == null) {
			return null;
		}
		return map.get(attributeName);
	}

	public Object removeAttribute(String attributeName) {
		if (map == null) {
			return null;
		}
		return map.remove(attributeName);
	}

}