<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
	
		<html>
			<head><title>Checkout</title></head>	
			
			<body>
				<h1>checkout (simplified XSLT view)</h1>
				
				<table>
					<thead><th>product</th><th>price</th></thead>
					<tbody>						
						<xsl:for-each select="cart/cartItem">
							<tr><td><xsl:value-of select="./item/productName"/></td><td><xsl:value-of select="@totalPrice"/></td></tr>
            			</xsl:for-each> 
					</tbody>
				</table>
				<hr/>
				Cart total: <xsl:value-of select="cart/@subTotal"/>
				
				<br/><br/>
				<a href="newOrder.do">Continue..</a>
			</body>
		</html>
	
	</xsl:template>
	
</xsl:stylesheet>