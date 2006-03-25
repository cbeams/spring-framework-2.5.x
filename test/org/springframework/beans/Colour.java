package org.springframework.beans;

import org.springframework.core.enums.ShortCodedLabeledEnum;

/**
 * @author Rob Harrop
 */
public class Colour extends ShortCodedLabeledEnum {

	public static final Colour RED = new Colour(0, "RED");
	public static final Colour BLUE = new Colour(1, "BLUE");
	public static final Colour GREEN = new Colour(2, "GREEN");
	public static final Colour PURPLE = new Colour(3, "PURPLE");

	private Colour(int code, String label) {
		super(code, label);
	}
}
