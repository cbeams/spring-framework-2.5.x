package org.springframework.samples.tiles;

/**
 * News item data class.
 * @author Alef Arendsen
 */
public class NewsItem {
	
	private String link;
	
	private String title;
	
	private String sourceName;
	
	public void setLink(String link) {
		this.link = link;		
	}
	
	public String getLink() {
		return this.link;
	}
	
	public void setTitle(String title) {
		this.title = title;		
	}

	public String getTitle() {
		return this.title;
	}
		
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;		
	}

	public String getSourceName() {
		return this.sourceName;
	}

}
