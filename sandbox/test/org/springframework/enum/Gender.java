/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/test/org/springframework/enum/Gender.java,v 1.1 2004-05-03 04:30:19 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-05-03 04:30:19 $
 *
 * Copyright Computer Science Innovations (CSI), 2003. All rights reserved.
 */
package org.springframework.enum;

/**
 * @author  Keith Donald
 */
public class Gender extends LetterCodedEnum {
    public Gender(char code) {
        super(code);
    }
}
