/*
 * $Header:
 * /usr/local/cvs/product/project/src/java/com/csi/product/project/Type.java,v
 * 1.1 2004/01/26 23:10:32 keith Exp $ $Revision: 1.1 $ $Date: 2004/01/26
 * 23:10:32 $
 * 
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.functor;

/**
 * @author Keith Donald
 */
public class LogicalOperator {
    public static final LogicalOperator AND = new LogicalOperator("and");
    public static final LogicalOperator OR = new LogicalOperator("or");
    private String name;

    private LogicalOperator(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}