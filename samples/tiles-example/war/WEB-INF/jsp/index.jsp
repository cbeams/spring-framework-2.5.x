<h2>Welcome to SomeCompany Inc.</h2>
<p>
	This is a simple example of the usage of Tiles in combination with Spring.
	Have a look at the <code>tiles-servlet.xml</code> and the reference manual 
	for more ifnromation on how to configure Tiles and use things like the 
	<code>ComponentControllerSupport</code>.
</p>
<p>
	This tiny site has a template (main.jsp, consisting of the top of the page
	including the menu) and three content JSPs (index.jsp, about.jsp and contact.jsp).
	By adding the UrlFileNameViewController from all URLs, the file name is used as
	the view name. The <code>InternalResourceViewResolver</code> uses the 
	<code>TilesJstlView</code>. Last but not least, a <code>TilesConfigurer</code>
	is used to configure the tiles from the <code>definitions.xml</code> file.
</p>

<p>
	Besides the templates described above, there's another template called <code>news</code>
	that automatically imports news from Slashdot, TheServerside or JavaBlogs (currently the items
	are NOT cached, so it can take a while before the feeds load). Those
	templates use the Tiles Controller notion as can be seen in the <code>definitions.xml</code> file
	By extending <code>ComponentControllerSupport</code>, you can access the application context.
</p>

