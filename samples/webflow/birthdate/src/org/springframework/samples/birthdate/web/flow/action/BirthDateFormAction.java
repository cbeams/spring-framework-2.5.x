package org.springframework.samples.birthdate.web.flow.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.FormAction;

public class BirthDateFormAction extends FormAction {

	// standard European format
	private static final String BIRTH_DATE_PATTERN = "dd-MM-yyyy";

	private static final String BIRTHDATE_FORM_OBJECT_NAME = "birthDate";

	private static final String AGE_NAME = "age";

	public BirthDateFormAction() {
		// tell the superclass about the form object and validator we want to
		// use you could also do this in the application context XML ofcourse
		setFormObjectName(BIRTHDATE_FORM_OBJECT_NAME);
		setFormObjectClass(BirthDate.class);
		setValidator(new BirthDateValidator());
	}

	protected void initBinder(RequestContext context, DataBinder binder) {
		// register a custom property editor to handle the date input
		SimpleDateFormat dateFormat = new SimpleDateFormat(BIRTH_DATE_PATTERN);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

	/*
	 * Our "onSubmit" hook: an action execute method.
	 */
	public Event calculateAge(RequestContext context) throws Exception {
		// pull the date from the model
		BirthDate birthDate = (BirthDate)context.getRequestScope().get(BIRTHDATE_FORM_OBJECT_NAME);

		// calculate the age (quick & dirty, probably has bugs :-)
		// in a real application you would delegate to the business layer for
		// this kind of logic
		Calendar calBirthDate = new GregorianCalendar();
		calBirthDate.setTime(birthDate.getDate());
		Calendar calNow = new GregorianCalendar();

		int ageYears = calNow.get(Calendar.YEAR) - calBirthDate.get(Calendar.YEAR);
		long ageMonths = calNow.get(Calendar.MONTH) - calBirthDate.get(Calendar.MONTH);
		long ageDays = calNow.get(Calendar.DAY_OF_MONTH) - calBirthDate.get(Calendar.DAY_OF_MONTH);

		if (ageDays < 0) {
			ageMonths--;
			ageDays += calBirthDate.getActualMaximum(Calendar.DAY_OF_MONTH);
		}

		if (ageMonths < 0) {
			ageYears--;
			ageMonths += 12;
		}

		// create a nice age string
		StringBuffer ageStr = new StringBuffer();
		ageStr.append(ageYears).append(" years, ");
		ageStr.append(ageMonths).append(" months and ");
		ageStr.append(ageDays).append(" days");

		// put it in the model for display by the view
		context.getRequestScope().setAttribute(AGE_NAME, ageStr);

		return success();
	}
}