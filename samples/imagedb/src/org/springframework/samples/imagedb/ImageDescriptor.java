package org.springframework.samples.imagedb;

/**
 * Simple data holder for image descriptions.
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class ImageDescriptor {

	private final String name;

	private final String description;

	protected ImageDescriptor(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getShortDescription() {
		return (description.length() <= 200) ? description : description.substring(0, 200);
	}

	public int getDescriptionLength() {
		return description.length();
	}

}
