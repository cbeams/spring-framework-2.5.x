package org.springframework.beans;

import java.io.Serializable;

import org.springframework.beans.factory.DisposableBean;

/**
 * @author Juergen Hoeller
 * @since 21.08.2003
 */
public class DerivedTestBean extends TestBean implements Serializable, DisposableBean {

	private boolean destroyed;

	public void destroy() {
		this.destroyed = true;
	}

	public boolean wasDestroyed() {
		return destroyed;
	}

}
