/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.mail;

import junit.framework.TestCase;
import org.springframework.mail.cos.CosMailSenderImpl;

/**
 * @author Andre Biryukov
 * @version $Id: CosMailTestSuite.java,v 1.2 2004-03-18 03:01:39 trisberg Exp $
 */
public class CosMailTestSuite extends TestCase{

    public void testWithSimpleMessageBadHostName() throws MailException{
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("a@a.com");
        message.setTo("b@b.com");
        message.setSubject("test");
        message.setText("another test");

        CosMailSenderImpl sender = new CosMailSenderImpl();
        sender.setHost("hostxyzdoesnotexist");
        try {
            sender.send(message);
            fail("the UnknownHostException should have been thrown unless you have host hostxyzdoesnotexist on your network");
        } catch (Exception e){
            assertTrue(e instanceof MailSendException);
        }

    }
}
