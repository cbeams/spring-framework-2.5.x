/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.util;

import java.io.Serializable;

import org.springframework.util.ToStringCreator;

/**
 * Holder for html image button data. Really just needed as a hack to prevent
 * struts from bombing on forms with multiple image buttons.
 * 
 * @author Keith Donald
 */
public class ImageButton implements Serializable {
    private int x = -1;

    private int y = -1;

    public boolean isSet() {
        return !(x == -1 && y == -1);
    }

    /**
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * @param x The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * @param y The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }
    
    public String toString() {
        return new ToStringCreator(this).appendProperties().toString();
    }
}