<h2>Welcome to SomeCompany Inc.</h2>
<p>
	This is a very simple example of the usage of Tiles in combination with Spring. 
	Have a look at the tiles-servlet.xml and the document in docs/tiles in the Spring 
	distro for more information on how to configure Tiles for use with Spring.	
	This tiny site has a template (main.jsp, consisting of the top of the page 
	including the menu) and three content JSPs (index.jsp, about.jsp and contact.jsp). 
	The FileNameViewController inspect the URL, strips of everything except for
	the filename and uses this as the definition name. In definitions.xml four 
	definitions are defined that make up the site.
</p>

<p>
	Besides the templates describes above, there's another template called <code>news</code>
	that automatically imports news from Slashdot, TheServerside or WorldPress. Those
	templates use the Tiles Controller notion as can be seen in the <code>definitions.xml</code> file
	By extending <code>ComponentControllerSupport</code>, you can access the application context.
</p>

<p>
	Todo, better documentation and inclusion of a detailed usage example (as mentioned in the
	first paragraph)
</p>

<p>
	Todo, implement some caching on the newsfeeds, it's a little bit slow right now.
</p>

<p>
	Toto, review code to do error handling and more comments!!!
</p>
