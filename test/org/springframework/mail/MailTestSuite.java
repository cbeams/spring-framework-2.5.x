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
 * @version $Id: MailTestSuite.java,v 1.2 2003-09-10 19:33:02 dkopylenko Exp $
 */
public class MailTestSuite extends TestCase {

	public void testBeanProperties() throws Exception {
		MockControl msControl = EasyMock.niceControlFor(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
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
		MockControl msControl = EasyMock.niceControlFor(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.activate();
		mt.setMailSender(ms);
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

	public void testNullMailSettings() throws Exception {
		MailTemplate mt = new MailTemplate();
		MockControl msControl = EasyMock.niceControlFor(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.activate();
		mt.setMailSender(ms);
		mt.setMailSettings(null);
		try {
			mt.sendMail(new MailCallback() {
				public void configure(MailSettings mailSettings) {
				}
			});
			fail("Should throw MailTemplateIllegalStateException");
		}
		catch (MailTemplateIllegalStateException ex) {
			//Expected
		}
	}

	public void testConfiguredMailSettings() throws Exception {
		MailTemplate mt = new MailTemplate();
		MockControl msControl = EasyMock.niceControlFor(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.activate();
		mt.setMailSender(ms);

		try {
			mt.sendMail(new MailCallback() {
				public void configure(MailSettings mailSettings) {
					mailSettings.setMailTo("xxx@yahoo.com");
					mailSettings.setMailFrom("xxx@org.springframework.com");
					mailSettings.setMailSubject("test");
					mailSettings.setMailText("test");
					mailSettings.setMailHost("localhost");
				}

			});
		}
		catch (IllegalStateException ex) {
			fail("Should not throw IllegalStateException");
		}
	}
}
