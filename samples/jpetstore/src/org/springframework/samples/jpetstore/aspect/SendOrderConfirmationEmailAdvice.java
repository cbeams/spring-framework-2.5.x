package org.springframework.samples.jpetstore.aspect;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.MethodAfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.logic.PetStoreFacade;

/**
 * AOP advice that sends confirmation email after order has been submitted
 * @author Dmitriy Kopylenko
 * @version $Id: SendOrderConfirmationEmailAdvice.java,v 1.1 2004-02-28 18:07:37 dkopylenko Exp $
 */
public class SendOrderConfirmationEmailAdvice implements MethodAfterReturningAdvice, InitializingBean {

	private MailSender mailSender;

	private static final String MAIL_FROM = "jpetstore@springframework.org";

	private static final String SUBJECT = "Thank you for your order!";

	private static final Log logger = LogFactory.getLog(SendOrderConfirmationEmailAdvice.class.getName());

	/** 
	 * @see org.springframework.aop.MethodAfterReturningAdvice#afterReturning(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	public void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable {
		Order order = (Order) args[0];
		Account account = ((PetStoreFacade) target).getAccount(order.getUsername());

		//Don't do anything if email address is not set
		if (account.getEmail() == null || account.getEmail().length() == 0) {
			return;
		}

		String text =
			"Dear "
				+ account.getFirstName()
				+ " "
				+ account.getLastName()
				+ ", thank your for your order from JPetstore. Please note that your order number is "
				+ order.getOrderId();

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(account.getEmail());
		mailMessage.setFrom(MAIL_FROM);
		mailMessage.setSubject(SUBJECT);
		mailMessage.setText(text);
		try {
			this.mailSender.send(mailMessage);
		}
		catch (MailException ex) {
			//just log it and go on
			logger.warn("An exception occured when trying to send email", ex);
		}

	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	/** 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.mailSender == null) {
			throw new IllegalStateException("mailSender property must be set");
		}
	}
}
