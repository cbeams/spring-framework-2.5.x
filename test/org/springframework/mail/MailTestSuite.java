/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail;

import org.easymock.EasyMock;
import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 * @author Dmitriy Kopylenko
 * @version $Id: MailTestSuite.java,v 1.1 2003-09-10 00:15:16 dkopylenko Exp $
 */
public class MailTestSuite extends TestCase {

	public void testBeanProperties() throws Exception {
		MockControl msControl = EasyMock.niceControlFor(MailSender.class);
		MailSender ms = (MailSender) msControl.getMock();
		msControl.activate();

		MailTemplate mt = new MailTemplate(ms);
		assertTrue("mailSender ok", mt.getMailSender() == ms);

		MailSettings mailSettings = new MailSettings();
		mt.setMailSettings(mailSettings);
		assertTrue("mailSettings ok", mt.getMailSettings() == mailSettings);
	}

	public void testNullMailSenderInContructor() throws Exception {
		try {
			MailTemplate mt = new MailTemplate(null);
			fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			//Expected
		}
	}

	public void testMissconfiguredMailSettings() throws Exception {
		MailTemplate mt = new MailTemplate();
		try {
			mt.sendMail(new MailCallback() {
				public void configure(MailSettings mailSettings) {
					mailSettings.setMailTo("xxx@yahoo.com");
					mailSettings.setMailFrom("xxx@org.springframework.com");
				}
			});
			fail("Should throw IllegalStateException");
		}
		catch (IllegalStateException ex) {
			//Expected
		}
	}

	public void testWithNullMailSettings() throws Exception {
		MailTemplate mt = new MailTemplate();
		mt.setMailSettings(null);
		try {
			mt.sendMail(new MailCallback() {
				public void configure(MailSettings mailSettings) {
					mailSettings.setMailTo("xxx@yahoo.com");
					mailSettings.setMailFrom("xxx@org.springframework.com");
				}
			});
			fail("Should throw MailTemplateIllegalStateException");
		}
		catch (MailTemplateIllegalStateException ex) {
			//Expected
		}
	}
}
