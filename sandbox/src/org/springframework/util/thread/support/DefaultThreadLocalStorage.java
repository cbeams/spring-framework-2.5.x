/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.thread.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.thread.ThreadCleanupBroadcaster;
import org.springframework.util.thread.ThreadCleanupListener;
import org.springframework.util.thread.ThreadLocalStorage;

public class DefaultThreadLocalStorage implements ThreadLocalStorage, ThreadCleanupListener {
	private static final String CREATED_KEY = DefaultThreadLocalStorage.class + ".created";

	private MapThreadLocal threadLocal = new MapThreadLocal();

	private ThreadCleanupBroadcaster cleanupBroadcaster;

	public void setCleanupBroadcaster(ThreadCleanupBroadcaster broadcaster) {
		this.cleanupBroadcaster = broadcaster;
	}

	private Map getThreadLocalStorageMap() {
		Map map = (Map)threadLocal.get();
		if (Boolean.TRUE.equals(map.get(CREATED_KEY)) && this.cleanupBroadcaster != null) {
			this.cleanupBroadcaster.addThreadCleanupListener(this);
			map.remove(CREATED_KEY);
		}
		return map;
	}

	public Object get(Object key) {
		Map map = getThreadLocalStorageMap();
		return map.get(key);
	}

	public void put(Object key, Object value) {
		Map map = getThreadLocalStorageMap();
		map.put(key, value);
	}

	public void clear() {
		Map map = (Map)threadLocal.get();
		if (map != null) {
			map.clear();
		}
	}

	public void onCleanup() {
		clear();
	}

	private static class MapThreadLocal extends ThreadLocal {
		protected Object initialValue() {
			Map map = new HashMap();
			map.put(CREATED_KEY, Boolean.TRUE);
			return map;
		}
	}
}