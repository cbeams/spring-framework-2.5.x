package org.springframework.samples.tiles;

import java.util.Properties;
import java.util.Set;

/**
 * NewsFeed configurator. See tiles-servlet.xml for more info.
 * @author Alef Arendsen.
 */
public class NewsFeedConfigurer {
	
	private Properties feeds;
	
	public void setFeeds(Properties feeds) {
		this.feeds = feeds;
	}
	
	public String feedUri(String feedName) {
		return feeds.getProperty(feedName);
	}
	
	public Set allNames() {
		return feeds.keySet();
	}

}
