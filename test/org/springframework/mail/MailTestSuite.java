/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * @author Dmitriy Kopylenko
 * @version $Id: MailTestSuite.java,v 1.3 2003-09-19 11:50:40 johnsonr Exp $
 */
public class MailTestSuite extends TestCase {

	public void testBeanProperties() throws Exception {
		MockControl msControl = MockControl.createNiceControl(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.replay();

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
		MockControl msControl = MockControl.createNiceControl(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.replay();
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
		MockControl msControl = MockControl.createNiceControl(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.replay();
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
		MockControl msControl = MockControl.createNiceControl(MailSender.class);
		MailSender ms = (MailSender)msControl.getMock();
		msControl.replay();
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
