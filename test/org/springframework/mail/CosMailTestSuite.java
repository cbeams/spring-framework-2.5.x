package org.springframework.mail;

import junit.framework.TestCase;
import org.springframework.mail.cos.CosMailSenderImpl;

/**
 * @author Andre Biryukov
 * @version $Id: CosMailTestSuite.java,v 1.1 2004-01-30 09:15:33 johnsonr Exp $
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
