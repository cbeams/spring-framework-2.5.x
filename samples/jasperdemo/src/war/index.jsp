<html>
	<head>
		<title>Jasper Reports Test</title>
	</head>
	<body>
        <h1>PDF Format</h1>
		<a href="simpleReport.pdf">Pre-Compiled Report File</a><br>
		<a href="simpleReportCompile.pdf">Dynamically Compiled Report File</a><br>
        <h1>Other Formats</h1>
		<a href="simpleReportHtml.html">HTML</a><br>
		<a href="simpleReportCsv.csv">CSV</a><br>
		<a href="simpleReportExcel.xls">Excel</a><br>
        <h1>Sub-Reports</h1>
        <a href="subReport.pdf">In PDF Format</a>
				
				<h1>Using Multi-Format View</h1>
				<a href="simpleReportMulti.pdf">PDF</a>
				<a href="simpleReportMulti.csv">CSV</a>
				<a href="simpleReportMulti.html">HTML</a>
				<a href="simpleReportMulti.xls">Excel</a>
				
				<h1>Using a POST</h1>
				<h2>With .pdf Extension</h2>
				<form name="testForm1" method="POST" action="simpleReportPost.pdf">
					<input name="reportTitle" value="foo" type="test"/>
					<input type="Submit" value="Try with a POST"/>
				</form>
				<h2>With .action Extension</h2>
				<form name="testForm2" method="POST" action="simpleReportPost.action">
					<input name="reportTitle" value="foo" type="test"/>
					<input type="Submit" value="Try with a POST"/>
				</form>
	</body>
</html>