/*
 * $Header: /usr/local/cvs/module/src/java/File.java,v 1.7 2004/01/16 22:23:11
 * keith Exp $ $Revision: 1.1 $ $Date: 2004-09-09 05:31:56 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.support;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.rules.Closure;
import org.springframework.rules.Generator;

public class IteratorGeneratorAdapter implements Generator {
    private Iterator it;

    public IteratorGeneratorAdapter(Collection collection) {
        this(collection.iterator());
    }

    public IteratorGeneratorAdapter(Iterator it) {
        this.it = it;
    }

    public void generate(Closure closure) {
        while (it.hasNext()) {
            closure.call(it.next());
        }
    }
}