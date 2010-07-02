package org.springframework.samples.tiles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.PreparerException;
import org.apache.tiles.preparer.ViewPreparer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Controller for the news tile that retrieves a feed
 * mentioned in the definitions file.
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 */
public class NewsFeedController implements ViewPreparer {

	private NewsFeedConfigurer configurer;


	public void setConfigurer(NewsFeedConfigurer configurer) {
		this.configurer = configurer;
	}

	public void execute(TilesRequestContext tilesRequestContext, AttributeContext attributeContext) throws PreparerException {
		String uri = this.configurer.feedUri((String) attributeContext.getAttribute("sourceName").getValue());
		try {
			NewsReader reader = new NewsReader(uri);
			int size = Integer.parseInt((String) attributeContext.getAttribute("size").getValue());
			List items = new ArrayList();
			for (int i = 0; i < size && i < reader.size(); i++) {
				NewsItem item = new NewsItem();
				item.setLink(reader.getLinkAt(i));
				item.setTitle(reader.getTitleAt(i));
				item.setSourceName((String) attributeContext.getAttribute("sourceName").getValue());
				items.add(item);
			}
			attributeContext.putAttribute("items", new Attribute(items));
		}
		catch (Exception ex) {
			throw new PreparerException(ex);
		}
	}


	private static class NewsReader {

		private Document feed;

		private String type;

		public NewsReader(String uriString) throws MalformedURLException, DocumentException {
			// get and parse the feed
			DocumentFactory docFactory = DocumentFactory.getInstance();
			SAXReader saxReader = new SAXReader(docFactory, false);
			URL uri = new URL(uriString);
			this.feed = saxReader.read(uri);
			// determine the type
			this.type = feed.getRootElement().getName();
		}

		private String getChannelPrefix() {
			if ("rss".equals(this.type)) {
				return "/*/*/";
			}
			else {
				return "/*/";
			}
		}

		public int size() {
			// todo: RDF somehow doesn't work with XPATH expression, fix it
			if ("RDF".equals(this.type)) {
				int size = 0;
				Iterator it = feed.getRootElement().elements().iterator();
				while (it.hasNext()) {
					String qName = ((Element) it.next()).getQualifiedName();
					if (qName.equals("item")) {
						size++;
					}
				}
				return size;
			}
			return this.feed.selectNodes(getChannelPrefix() + "item").size();
		}

		public String getItemAsXML(int index) {
			Node node = null;

			// determine RDF or other format
			if ("RDF".equals(this.type)) {
				node = getRDFNodeAt(index, null);
			}
			else {
				node = this.feed.selectSingleNode(getChannelPrefix() + "item[" + (index + 1) + "]");
			}

			// return as XML
			if (node != null) {
				return node.asXML();
			}

			return null;
		}

		public String getChannelTitle() {
			return this.feed.getRootElement().element("channel").element("title").getText();
		}

		public String getChannelLink() {
			return this.feed.getRootElement().element("channel").element("link").getText();
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
			if ("RDF".equals(this.type)) {
				value = getRDFNodeAt(index, nodeName).getText();
			}
			else {
				value = this.feed.selectSingleNode(
						getChannelPrefix() + "item[" + (index + 1) + "]/" + nodeName).getText();
			}

			return value;
		}

		private List getRDFItemNodes() {
			// iterate through the entire document in search for item nodes
			List items = new ArrayList(this.feed.getRootElement().elements().size());
			Iterator it = this.feed.getRootElement().elements().iterator();
			while (it.hasNext()) {
				Element elem = (Element) it.next();
				String qName = elem.getQualifiedName();
				if ("item".equals(qName)) {
					// found one, increment size
					items.add(elem);
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
