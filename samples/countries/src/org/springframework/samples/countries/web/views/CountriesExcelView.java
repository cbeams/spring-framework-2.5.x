package org.springframework.samples.countries.web.views;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.springframework.beans.support.RefreshablePagedListHolder;
import org.springframework.beans.support.SortDefinition;
import org.springframework.context.NoSuchMessageException;
import org.springframework.samples.countries.appli.ICountry;
import org.springframework.samples.countries.web.CountriesFilter;
import org.springframework.web.servlet.view.document.AbstractExcelView;

/**
 * This view demonstrates how to send an Excel file with the Spring Framework
 * using the jakarta's POI library.
 * <br>Here create a document from scratch, but it is also possible to start from a template
 * document. In this case, add an url property in the view definition like:
 * <br>countries_excelView.url=/WEB-INF/views/excel/countries
 * <br>Creating the directories, put an excel file '/WEB-INF/views/excel/countries.xls', it will be taken
 * as a starting point.
 * <br>You can also add in the same directory files like 'countries_en.xls', 'countries_fr.xls' and so on.
 * Theses files will take precedence if the user's locale matches.
 *
 * @author Jean-Pierre Pawlak
 */
public class CountriesExcelView extends AbstractExcelView {

	protected void buildExcelDocument(
			Map model, HSSFWorkbook wb, HttpServletRequest request, HttpServletResponse response)
			throws NoSuchMessageException {

		// We search the data to insert.
		RefreshablePagedListHolder pgHolder = (RefreshablePagedListHolder) model.get("countries");
		Locale loc = pgHolder.getLocale();

		// As we use a from scratch document, we create a new sheet.
		HSSFSheet sheet = wb.createSheet("SPRING-Countries");
		// If we will use the first sheet from an existing document, replace by this:
		// sheet = wb.getSheetAt(0);

		// We simply put an error message on the first cell if no list is available
		// Nevertheless, it should never be null as the controller verify it.
		if (null == pgHolder) {
			getCell(sheet, 0, 0).setCellValue(getMessageSourceAccessor().getMessage("nolist", loc));
			return;
		}

		// We create a font for headers
		HSSFFont f = wb.createFont();
		// set font 1 to 12 point type
		f.setFontHeightInPoints((short) 12);
		// make it blue
		f.setColor((short) 0xc);
		// make it bold arial is the default font
		f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		// We create a style for headers
		HSSFCellStyle cs = wb.createCellStyle();
		cs.setFont(f);
		cs.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		// The same for properties data
		HSSFFont fp = wb.createFont();
		fp.setColor((short) 0xc);
		HSSFCellStyle csp = wb.createCellStyle();
		csp.setFont(fp);
		csp.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		// We create a date style
		HSSFCellStyle dateStyle = wb.createCellStyle();
		dateStyle.setFont(fp);
		dateStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

		// We set the colum width of the two first columns
		sheet.setColumnWidth((short) 0, (short) (20 * 256));
		sheet.setColumnWidth((short) 1, (short) (20 * 256));

		// We prepare some data
		SortDefinition sort = pgHolder.getSort();
		CountriesFilter filter = (CountriesFilter) pgHolder.getFilter();

		int row = 0;

		// We put some information about the user request on the sheet
		// getCell is a useful add-on provided by the AbstractExcelView
		// The labels could be pre-inserted in a template document
		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("date.extraction", loc));
		getCell(sheet, row, 1).setCellValue(pgHolder.getRefreshDate());
		getCell(sheet, row, 1).setCellStyle(dateStyle);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("nbRecords", loc));
		getCell(sheet, row, 1).setCellValue(pgHolder.getNrOfElements());
		getCell(sheet, row, 1).setCellStyle(csp);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("sort.name", loc));
		getCell(sheet, row, 1).setCellValue(getMessageSourceAccessor().getMessage(sort.getProperty(), "", loc));
		getCell(sheet, row, 1).setCellStyle(csp);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("sort.asc", loc));
		getCell(sheet, row, 1).setCellValue(getMessageSourceAccessor().getMessage(new Boolean(sort.isAscending()).toString(), loc));
		getCell(sheet, row, 1).setCellStyle(csp);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("sort.igncase", loc));
		getCell(sheet, row, 1).setCellValue(getMessageSourceAccessor().getMessage(new Boolean(sort.isIgnoreCase()).toString(), loc));
		getCell(sheet, row, 1).setCellStyle(csp);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("filter.name", loc));
		getCell(sheet, row, 1).setCellValue(filter.getName());
		getCell(sheet, row, 1).setCellStyle(csp);
		row++;

		getCell(sheet, row, 0).setCellValue(getMessageSourceAccessor().getMessage("filter.code", loc));
		getCell(sheet, row, 1).setCellValue(filter.getCode());
		getCell(sheet, row, 1).setCellStyle(csp);
		// row += 3;

		// We create a second shhet for the data
		sheet = wb.createSheet(getMessageSourceAccessor().getMessage("countries", loc));
		sheet.setColumnWidth((short) 1, (short) (30 * 256));
		row = 0;

		// We put now the headers of the list on the sheet
		HSSFCell cell = getCell(sheet, row, 0);
		cell.setCellStyle(cs);
		cell.setCellValue(getMessageSourceAccessor().getMessage("code", loc));
		cell = getCell(sheet, row, 1);
		cell.setCellStyle(cs);
		cell.setCellValue(getMessageSourceAccessor().getMessage("name", loc));
		row++;

		// We put now the countries from the list on the sheet
		Iterator it = pgHolder.getSource().iterator();
		while (it.hasNext()) {
			ICountry country = (ICountry) it.next();
			getCell(sheet, row, 0).setCellValue(country.getCode());
			getCell(sheet, row, 1).setCellValue(country.getName());
			row++;
		}
	}

}
