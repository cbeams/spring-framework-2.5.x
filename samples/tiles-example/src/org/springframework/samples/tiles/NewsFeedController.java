package org.springframework.samples.tiles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.tiles.ComponentContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.view.tiles.ComponentControllerSupport;

/**
 * Controller for the news tile that retrieves a feed
 * mentioned in the definitions file.
 * @author Alef Arendsen
 */
public class NewsFeedController extends ComponentControllerSupport {

	private Log log = LogFactory.getLog(NewsFeedController.class);
	
	protected void doPerform(ComponentContext componentContext, HttpServletRequest request,
													 HttpServletResponse response) {

		ApplicationContext ctx = getApplicationContext();
		NewsFeedConfigurer configurer = (NewsFeedConfigurer) ctx.getBean("feedConfigurer");
		String uri = configurer.feedUri((String) componentContext.getAttribute("sourceName"));
		NewsReader reader = new NewsReader(uri);

		int size = Integer.parseInt((String) componentContext.getAttribute("size"));
		List items = new ArrayList();
		for (int i = 0; i < size && i < reader.size(); i++) {
			NewsItem item = new NewsItem();
			item.setLink(reader.getLinkAt(i));
			item.setTitle(reader.getTitleAt(i));
			item.setSourceName((String) componentContext.getAttribute("sourceName"));
			items.add(item);
		}
		
		componentContext.putAttribute("items", items);
	}


	private static class NewsReader {

		private Document feed;

		private String type = null;

		private static final String RSS = "rss";

		public NewsReader(String uriString) {
			// get and parse the feed
			try {
				DocumentFactory docFactory = DocumentFactory.getInstance();
				SAXReader saxReader = new SAXReader(docFactory, false);
				URL uri = new URL(uriString);
				feed = saxReader.read(uri);
				// determine the type
				type = feed.getRootElement().getName();
			}
			catch (MalformedURLException e) {
				// @todo do something
			}
			catch (DocumentException e) {
				// @todo do something
			}
		}

		private String getChannelPrefix() {
			if (RSS.equals(type)) {
				return "/*/*/";
			}
			else {
				return "/*/";
			}
		}

		public int size() {
			// todo: RDF somehow doesn't work with XPATH expression, fix it
			if (type.equals("RDF")) {
				int size = 0;
				Iterator itr = feed.getRootElement().elements().iterator();
				while (itr.hasNext()) {
					String qName = ((Element) itr.next()).getQualifiedName();
					if (qName.equals("item")) {
						size++;
					}
				}

				return size;
			}

			return feed.selectNodes(getChannelPrefix() + "item").size();
		}

		public String getItemAsXML(int index) {
			Node node = null;

			// determine RDF or other format
			if (type.equals("RDF")) {
				node = getRDFNodeAt(index, null);
			}
			else {
				node = feed.selectSingleNode(getChannelPrefix() + "item[" + (index + 1) + "]");
			}

			// return as xml
			if (node != null) {
				return node.asXML();
			}

			return null;
		}

		public String getChannelTitle() {
			return feed.getRootElement().element("channel").element("title").getText();
		}

		public String getChannelLink() {
			return feed.getRootElement().element("channel").element("link").getText();
		}

		public String getLinkAt(int i) {
			return getItemNodeValue(i, "link");
		}

		public String getTitleAt(int i) {
			return getItemNodeValue(i, "title");
		}

		public String getDescriptionAt(int i) {
			return getItemNodeValue(i, "description");
		}

		private String getItemNodeValue(int index, String nodeName) {
			String value = null;

			// determine RDF or other format
			if (type.equals("RDF")) {
				value = getRDFNodeAt(index, nodeName).getText();
			}
			else {
				value = feed.selectSingleNode(getChannelPrefix() + "item[" + (index + 1) + "]" +
																			"/" + nodeName).getText();
			}

			return value;
		}

		private List getRDFItemNodes() {
			// iterate through the entire document in search for item nodes
			List items = new ArrayList(feed.getRootElement().elements().size());
			Iterator itr = feed.getRootElement().elements().iterator();
			while (itr.hasNext()) {
				Element e = (Element) itr.next();
				String qName = e.getQualifiedName();
				if (qName.equals("item")) {
					// found one, increment size
					items.add(e);
				}
			}

			return items;
		}

		private Node getRDFNodeAt(int i, String subNode) {
			// get all the item nodes
			List items = getRDFItemNodes();

			// find the correct item, and either return a 'subNode' or itself
			Element item = (Element) items.get(i);
			if (subNode == null) {
				return item;
			}
			else {
				return item.element(subNode);
			}
		}
	}

}
