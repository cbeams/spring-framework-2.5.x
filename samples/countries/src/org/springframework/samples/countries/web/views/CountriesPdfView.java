package org.springframework.samples.countries.web.views;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.support.RefreshablePagedListHolder;
import org.springframework.beans.support.SortDefinition;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.samples.countries.appli.ICountry;
import org.springframework.samples.countries.web.CountriesFilter;
import org.springframework.web.servlet.view.document.AbstractPdfView;

/**
 * This view demonstrates how to send a PDF file with the Spring Framework
 * using the iText PDF library.
 *
 * @author Jean-Pierre Pawlak
 */
public class CountriesPdfView extends AbstractPdfView {

	private static final Font HEADLINE_FONT = new Font( Font.HELVETICA, 18, Font.BOLD, Color.blue );
	private static final Font HEADING_FONT = new Font( Font.HELVETICA, 12, Font.ITALIC, Color.black );
	private static final Font HEADING_DATA_FONT = new Font( Font.HELVETICA, 12, Font.ITALIC, Color.blue );
	private static final Font DATA_HEAD_FONT = new Font( Font.HELVETICA, 10, Font.ITALIC, Color.black );
	private static final Font TEXT_FONT = new Font( Font.TIMES_ROMAN, 8, Font.NORMAL, Color.black );
	private static final Font BOLD_FONT = new Font( Font.TIMES_ROMAN, 8, Font.BOLD, Color.black );
	private static final int MARGIN = 32;

	protected void buildPdfMetadata(Map model, Document document, HttpServletRequest request) {
		document.addTitle("Countries List");
		document.addCreator("SPRING-Countries");
	}

	protected void buildPdfDocument(Map model, Document document,	PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response)
			throws DocumentException, NoSuchMessageException {

		// We search the data to insert.
		RefreshablePagedListHolder pgHolder = (RefreshablePagedListHolder) model.get("countries");
		Locale loc = pgHolder.getLocale();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, loc);

		// We prepare some data.
		SortDefinition sort = pgHolder.getSort();
		CountriesFilter filter = (CountriesFilter) pgHolder.getFilter();

		// We create and add the event handler.
		// So we can well paging, ensuring that only entire cells are printed
		// at end of pages (the latter is useless in this example as records
		// keep in one row, but in your own developpment it's not always the case).
		MyPageEvents events = new MyPageEvents(getMessageSourceAccessor(), loc);
		writer.setPageEvent(events);
		events.onOpenDocument(writer, document);
		
		String title = getMessageSourceAccessor().getMessage("app.name", loc);
		document.add(new Paragraph(title, HEADLINE_FONT));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));
	
		// We create a table for used criteria and extracting information
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(50);
		table.getDefaultCell().setBorderWidth(1);
		table.getDefaultCell().setBorderColor(Color.black);
		table.getDefaultCell().setPadding(4);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment( Element.ALIGN_MIDDLE);

		PdfPCell cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage("criteria", loc), HEADING_FONT));
		cell.setColspan(2);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setGrayFill(0.7f);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "property",loc), HEADING_FONT));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setGrayFill(0.9f);
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "value", loc), HEADING_FONT));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setGrayFill(0.9f);
		table.addCell(cell);

		// We put the used criteria and extracting information	
		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "date.extraction", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(df.format(pgHolder.getRefreshDate()), HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "nbRecords", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(String.valueOf(pgHolder.getNrOfElements()), HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "sort.name", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(sort.getProperty(), HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "sort.asc", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage(new Boolean(sort.isAscending()).toString(), loc),
				HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "sort.igncase", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage(new Boolean(sort.isIgnoreCase()).toString(), loc),
				HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "filter.name", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(null == filter.getName() ? "" : filter.getName(), HEADING_DATA_FONT));
		table.addCell(cell);

		cell = new PdfPCell(new Phrase(getMessageSourceAccessor().getMessage( "filter.code", loc), HEADING_FONT));
		table.addCell(cell);
		cell = new PdfPCell(new Phrase(null == filter.getCode() ? "" : filter.getCode(), HEADING_DATA_FONT));
		table.addCell(cell);

		document.add(table);
		document.newPage();

		// We can go now on the countries list
		table = new PdfPTable(2);
		int headerwidths[] = {20, 80};
		table.setWidths(headerwidths);
		table.setWidthPercentage(60);
		table.getDefaultCell().setBorderWidth(2);
		table.getDefaultCell().setBorderColor(Color.black);
		table.getDefaultCell().setGrayFill(0.75f);
		table.getDefaultCell().setPadding(3);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

		table.addCell(new Phrase(getMessageSourceAccessor().getMessage( "code", loc), DATA_HEAD_FONT));
		table.addCell(new Phrase(getMessageSourceAccessor().getMessage( "name", loc), DATA_HEAD_FONT));

		// We set the above row as remaining title
		// and adjust properties for normal cells
		table.setHeaderRows(1);
		table.getDefaultCell().setBorderWidth(1);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
	
		// We iterate now on the countries list	
		boolean even = false;
		Iterator it = pgHolder.getSource().iterator();  
		while(it.hasNext()) {
			if (even) {
				table.getDefaultCell().setGrayFill(0.95f);
				even = false;
			} else {
				table.getDefaultCell().setGrayFill(1.00f);
				even = true;
			}
			ICountry country = (ICountry)it.next();
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(new Phrase(country.getCode(), BOLD_FONT));
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
			table.addCell(new Phrase(country.getName(), TEXT_FONT));
		}
		document.add(table);
	}


	//~ Inner Classes ----------------------------------------------------------

	private static class MyPageEvents extends PdfPageEventHelper {

		private MessageSourceAccessor messageSourceAccessor;

		// this the locale to use for retrieved messages
		private Locale loc;

		// This is the contentbyte object of the writer
		PdfContentByte cb;

		// we will put the final number of pages in a template
		PdfTemplate template;

		// this is the BaseFont we are going to use for the header / footer
		BaseFont bf = null;
		
		public MyPageEvents(MessageSourceAccessor messageSourceAccessor, Locale loc) {
			this.messageSourceAccessor = messageSourceAccessor;
			this.loc = loc;
		}

		// we override the onOpenDocument method
		public void onOpenDocument(PdfWriter writer, Document document) {
			try	{
				bf = BaseFont.createFont( BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED );
				cb = writer.getDirectContent();
				template = cb.createTemplate(50, 50);
			} catch (DocumentException de) {
			} catch (IOException ioe) {}
		}

		// we override the onEndPage method
		public void onEndPage(PdfWriter writer, Document document) {
			int pageN = writer.getPageNumber();
			String text = messageSourceAccessor.getMessage("page", "page", loc) + " " + pageN + " " +
			    messageSourceAccessor.getMessage("on", "on", loc) + " ";
			float  len = bf.getWidthPoint( text, 8 );
			cb.beginText();
			cb.setFontAndSize(bf, 8);

			cb.setTextMatrix(MARGIN, 16);
			cb.showText(text);
			cb.endText();

			cb.addTemplate(template, MARGIN + len, 16);
			cb.beginText();
			cb.setFontAndSize(bf, 8);

			cb.endText();
		}

		// we override the onCloseDocument method
		public void onCloseDocument(PdfWriter writer, Document document) {
			template.beginText();
			template.setFontAndSize(bf, 8);
			template.showText(String.valueOf( writer.getPageNumber() - 1 ));
			template.endText();
		}
	}

}
