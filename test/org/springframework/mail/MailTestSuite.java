/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;

import junit.framework.TestCase;

import org.springframework.mail.javamail.JavaMailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @version $Id: MailTestSuite.java,v 1.6 2003-10-08 08:42:13 jhoeller Exp $
 */
public class MailTestSuite extends TestCase {

	public void testSimpleMessage() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("me@mail.org");
		message.setTo("you@mail.org");
		message.setCc(new String[] {"he@mail.org", "she@mail.org"});
		message.setSubject("my subject");
		message.setText("my text");

		assertEquals("me@mail.org", message.getFrom());
		assertEquals("you@mail.org", message.getTo());
		List ccs = Arrays.asList(message.getCc());
		assertTrue(ccs.contains("he@mail.org"));
		assertTrue(ccs.contains("she@mail.org"));
		assertEquals("my subject", message.getSubject());
		assertEquals("my text", message.getText());

		SimpleMailMessage messageCopy = new SimpleMailMessage(message);
		assertEquals("me@mail.org", messageCopy.getFrom());
		assertEquals("you@mail.org", messageCopy.getTo());
		ccs = Arrays.asList(messageCopy.getCc());
		assertTrue(ccs.contains("he@mail.org"));
		assertTrue(ccs.contains("she@mail.org"));
		assertEquals("my subject", messageCopy.getSubject());
		assertEquals("my text", messageCopy.getText());
	}

	public void testJavaMailSenderWithSimpleMessage() throws MailException, MessagingException, IOException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		SimpleMailMessage simpleMessage = new SimpleMailMessage();
		simpleMessage.setFrom("me@mail.org");
		simpleMessage.setTo("you@mail.org");
		simpleMessage.setCc(new String[] {"he@mail.org", "she@mail.org"});
		simpleMessage.setSubject("my subject");
		simpleMessage.setText("my text");
		sender.send(simpleMessage);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());

		assertEquals(1, sender.transport.getSentMessages().size());
		MimeMessage sentMessage = sender.transport.getSentMessage(0);
		List froms = Arrays.asList(sentMessage.getFrom());
		assertEquals(1, froms.size());
		assertEquals("me@mail.org", ((InternetAddress) froms.get(0)).getAddress());
		List tos = Arrays.asList(sentMessage.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos.size());
		assertEquals("you@mail.org", ((InternetAddress) tos.get(0)).getAddress());
		List ccs = Arrays.asList(sentMessage.getRecipients(Message.RecipientType.CC));
		assertEquals(2, ccs.size());
		assertEquals("he@mail.org", ((InternetAddress) ccs.get(0)).getAddress());
		assertEquals("she@mail.org", ((InternetAddress) ccs.get(1)).getAddress());
		assertEquals("my subject", sentMessage.getSubject());
		assertEquals("my text", sentMessage.getContent());
	}

	public void testJavaMailSenderWithSimpleMessages() throws MailException, MessagingException, IOException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		SimpleMailMessage simpleMessage1 = new SimpleMailMessage();
		simpleMessage1.setTo("he@mail.org");
		SimpleMailMessage simpleMessage2 = new SimpleMailMessage();
		simpleMessage2.setTo("she@mail.org");
		sender.send(new SimpleMailMessage[] {simpleMessage1, simpleMessage2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());

		assertEquals(2, sender.transport.getSentMessages().size());
		MimeMessage sentMessage1 = sender.transport.getSentMessage(0);
		List tos1 = Arrays.asList(sentMessage1.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos1.size());
		assertEquals("he@mail.org", ((InternetAddress) tos1.get(0)).getAddress());
		MimeMessage sentMessage2 = sender.transport.getSentMessage(1);
		List tos2 = Arrays.asList(sentMessage2.getRecipients(Message.RecipientType.TO));
		assertEquals(1, tos2.size());
		assertEquals("she@mail.org", ((InternetAddress) tos2.get(0)).getAddress());
	}

	public void testJavaMailSenderWithMimeMessage() throws MailException, MessagingException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage = sender.createMimeMessage();
		mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
		sender.send(mimeMessage);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage, sender.transport.getSentMessage(0));
	}

	public void testJavaMailSenderWithMimeMessages() throws MailException, MessagingException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage1 = sender.createMimeMessage();
		mimeMessage1.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
		MimeMessage mimeMessage2 = sender.createMimeMessage();
		mimeMessage2.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));
		sender.send(new MimeMessage[] {mimeMessage1, mimeMessage2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(2, sender.transport.getSentMessages().size());
		assertEquals(mimeMessage1, sender.transport.getSentMessage(0));
		assertEquals(mimeMessage2, sender.transport.getSentMessage(1));
	}

	public void testJavaMailSenderWithMimeMessagePreparator() throws MailException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		final List messages = new ArrayList();

		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("you@mail.org"));
				messages.add(mimeMessage);
			}
		};
		sender.send(preparator);

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(1, sender.transport.getSentMessages().size());
		assertEquals(messages.get(0), sender.transport.getSentMessage(0));
	}

	public void testJavaMailSenderWithMimeMessagePreparators() throws MailException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		final List messages = new ArrayList();

		MimeMessagePreparator preparator1 = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
				messages.add(mimeMessage);
			}
		};
		MimeMessagePreparator preparator2 = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));
				messages.add(mimeMessage);
			}
		};
		sender.send(new MimeMessagePreparator[] {preparator1, preparator2});

		assertEquals(sender.transport.getConnectedHost(), "host");
		assertEquals(sender.transport.getConnectedUsername(), "username");
		assertEquals(sender.transport.getConnectedPassword(), "password");
		assertTrue(sender.transport.isCloseCalled());
		assertEquals(2, sender.transport.getSentMessages().size());
		assertEquals(messages.get(0), sender.transport.getSentMessage(0));
		assertEquals(messages.get(1), sender.transport.getSentMessage(1));
	}

	public void testJavaMailSenderWithParseExceptionOnSimpleMessage() throws MailException {
		MockJavaMailSender sender = new MockJavaMailSender();
		SimpleMailMessage simpleMessage = new SimpleMailMessage();
		simpleMessage.setFrom("");
		try {
			sender.send(simpleMessage);
		}
		catch (MailParseException ex) {
			// expected
			assertTrue(ex.getRootCause() instanceof AddressException);
		}
	}

	public void testJavaMailSenderWithParseExceptionOnMimeMessagePreparator() throws MailException {
		MockJavaMailSender sender = new MockJavaMailSender();
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setFrom(new InternetAddress(""));
			}
		};
		try {
			sender.send(preparator);
		}
		catch (MailParseException ex) {
			// expected
			assertTrue(ex.getRootCause() instanceof AddressException);
		}
	}

	public void testJavaMailSendException() throws MessagingException, IOException, MailException {
		MockJavaMailSender sender = new MockJavaMailSender();
		sender.setHost("host");
		sender.setUsername("username");
		sender.setPassword("password");

		MimeMessage mimeMessage1 = sender.createMimeMessage();
		mimeMessage1.setRecipient(Message.RecipientType.TO, new InternetAddress("he@mail.org"));
		mimeMessage1.setSubject("fail");
		MimeMessage mimeMessage2 = sender.createMimeMessage();
		mimeMessage2.setRecipient(Message.RecipientType.TO, new InternetAddress("she@mail.org"));

		try {
			sender.send(new MimeMessage[] {mimeMessage1, mimeMessage2});
		}
		catch (JavaMailSendException ex) {
			assertEquals(sender.transport.getConnectedHost(), "host");
			assertEquals(sender.transport.getConnectedUsername(), "username");
			assertEquals(sender.transport.getConnectedPassword(), "password");
			assertTrue(sender.transport.isCloseCalled());
			assertEquals(1, sender.transport.getSentMessages().size());
			assertEquals(mimeMessage2, sender.transport.getSentMessage(0));
			assertEquals(1, ex.getFailedMimeMessages().size());
			assertEquals(mimeMessage1, ex.getFailedMimeMessages().keySet().iterator().next());
			Object subEx = ex.getFailedMimeMessages().values().iterator().next();
			assertTrue(subEx instanceof MessagingException);
			assertEquals("failed", ((MessagingException) subEx).getMessage());
		}
	}


	private static class MockJavaMailSender extends JavaMailSenderImpl {

		private final MockTransport transport = new MockTransport(session, null);

		protected Transport getTransport() throws NoSuchProviderException {
			return transport;
		}
	}


	private static class MockTransport extends Transport {

		private String connectedHost = null;
		private String connectedUsername = null;
		private String connectedPassword = null;
		private boolean closeCalled = false;
		private List sentMessages = new ArrayList();

		public MockTransport(Session session, URLName urlName) {
			super(session, urlName);
		}

		public String getConnectedHost() {
			return connectedHost;
		}

		public String getConnectedUsername() {
			return connectedUsername;
		}

		public String getConnectedPassword() {
			return connectedPassword;
		}

		public boolean isCloseCalled() {
			return closeCalled;
		}

		public List getSentMessages() {
			return sentMessages;
		}

		public MimeMessage getSentMessage(int index) {
			return (MimeMessage) this.sentMessages.get(index);
		}

		public void connect(String host, String username, String password) throws MessagingException {
			this.connectedHost = host;
			this.connectedUsername = username;
			this.connectedPassword = password;
		}

		public synchronized void close() throws MessagingException {
			this.closeCalled = true;
		}

		public void sendMessage(Message message, Address[] addresses) throws MessagingException {
			if ("fail".equals(message.getSubject())) {
				throw new MessagingException("failed");
			}
			List addr1 = Arrays.asList(message.getAllRecipients());
			List addr2 = Arrays.asList(addresses);
			if (!addr1.equals(addr2)) {
				throw new MessagingException("addresses not correct");
			}
			this.sentMessages.add(message);
		}
	}

}
