package org.springframework.load;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * XML reporter bean that writes an XML report and runs a stylesheet
 * to generate a formatted file.
 * @author Rod Johnson
 */
public class XmlTestReporter implements TestReporter {
	
	private String xmlReportFileName;
	
	private String stylesheet;
	
	private String outputFile;
	
	private DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
	
	public XmlTestReporter() {
		setDoubleFormat("###.##");
	}
	
	public final DecimalFormat getDecimalFormat() {
		return this.decimalFormat;
	}

	/**
	 * Set the decimal format used to format doubles such as the 
	 * number of hits per second
	 */
	public final void setDoubleFormat(String pattern) {
		this.decimalFormat.applyPattern(pattern);
	}
	

	/**
	 * @see org.springframework.load.TestReporter#generateReport(AbstractTestSuite)
	 */
	public void generateReport(AbstractTestSuite suite) throws Exception {
		
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			Document doc = db.newDocument();
			Element rootEle = doc.createElement("load-tests");
			doc.appendChild(rootEle);
			rootEle.setAttribute("date", new Date().toString());
			addTestStatusAttributes(suite, rootEle);
			
			rootEle.setAttribute("threads", "" + suite.getThreads());
//			rootEle.setAttribute("fails", "" + suite.getf)
			

			for (int i = 0; i < suite.getTests().length; i++) {
				Test test = suite.getTests()[i];
				
				Element testEle = doc.createElement("test");
				rootEle.appendChild(testEle);
				addTestStatusAttributes(test, testEle);
				
				if (test.getFailureExceptions().length > 0) {
					Element failuresEle = doc.createElement("failures");
					testEle.appendChild(failuresEle);
					for (int j = 0; j < test.getFailureExceptions().length; j++) {
						Element failEle = doc.createElement("fail");
						failuresEle.appendChild(failEle);
						Text t = doc.createTextNode(test.getFailureExceptions()[j].getLocalizedMessage());
						failEle.appendChild(t);
					}
				}
				
			}
			
			
			
			//String xmlstr = doc.getDocumentElement().toString();

			//System.out.println(xmlstr);
			
			System.out.println("Filename ='" + this.xmlReportFileName + "'");
			//FileWriter fw = new FileWriter(this.fileName);
			//fw.write(xmlstr);
			FileOutputStream fos = new FileOutputStream(this.xmlReportFileName);
			
			doTransform(new DOMSource(doc), fos, null);
			
			fos.close();
			System.out.println("Wrote XML report to '" + this.xmlReportFileName + "'");
			
			if (this.stylesheet != null && this.outputFile != null) {
				File f = new File(this.outputFile);
			
				Source xml = new DOMSource(doc);
				doTransform(xml, new FileOutputStream(f), new StreamSource(new FileInputStream(this.stylesheet)));
			
				fos.close();
			}
				
		}
		catch (Exception ex) {
			throw new RuntimeException("XML failure " + ex);
		}
		
	}
	
	
	private void addTestStatusAttributes(TestStatus test, Element testEle) {
		testEle.setAttribute("completed", "" + test.isComplete());
		testEle.setAttribute("passes",  "" + test.getPasses());
		testEle.setAttribute("name", "" + test.getName());
		testEle.setAttribute("failures", "" + test.getErrorCount());
		testEle.setAttribute("averageResponseTime", "" + test.getAverageResponseTime());
		testEle.setAttribute("hitsPerSecond", this.decimalFormat.format(test.getTestsPerSecondCount()));
		testEle.setAttribute("elapsedTime", "" + test.getElapsedTime());
		testEle.setAttribute("pauseTime", "" + test.getTotalPauseTime());
		testEle.setAttribute("totalWorkingTime", "" + test.getTotalWorkingTime());
		
		testEle.setAttribute("class", test.getClass().getName());
	}
	
	private void doTransform(Source xml, OutputStream os, Source ss) throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = (ss == null) ? tf.newTransformer() : tf.newTransformer(ss);

		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		trans.transform(xml, new StreamResult(os));
	}

	/**
	 * Returns the fileName.
	 * @return String
	 */
	public String getXmlReportFileName() {
		return xmlReportFileName;
	}

	/**
	 * Sets the fileName.
	 * @param fileName The fileName to set
	 */
	public void setXmlReportFileName(String fileName) {
		this.xmlReportFileName = fileName;
	}

	/**
	 * Returns the outputFile.
	 * @return String
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Returns the stylesheet.
	 * @return String
	 */
	public String getStylesheet() {
		return stylesheet;
	}

	/**
	 * Sets the outputFile.
	 * @param outputFile The outputFile to set
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Sets the stylesheet.
	 * @param stylesheet The stylesheet to set
	 */
	public void setStylesheet(String stylesheet) {
		this.stylesheet = stylesheet;
	}



}
