package org.springframework.samples.countries.web;

import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListSourceProvider;
import org.springframework.beans.support.RefreshablePagedListHolder;
import org.springframework.samples.countries.appli.ICountry;
import org.springframework.samples.countries.dao.IDaoCountry;
import org.springframework.validation.BindException;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author Jean-Pierre Pawlak
 */
public class CountriesController extends MultiActionController {

	private static final String COUNTRIES_ATTR = "countries";
	private static final String HOME_VIEW = "homeView";
	private static final String CONFIG_VIEW = "configView";
	private static final String MAIN_VIEW = "countries_mainView";
	private static final String DETAIL_VIEW = "countries_detailView";
	private static final String EXCEL_VIEW = "countries_excelView";
	private static final String PDF_VIEW = "countries_pdfView";

	private IDaoCountry daoCountry;
	private IDaoCountry secondDaoCountry;

	private String homeView = HOME_VIEW;
	private String configView = CONFIG_VIEW;
	private String mainView = MAIN_VIEW;
	private String detailView = DETAIL_VIEW;
	private String excelView = EXCEL_VIEW;
	private String pdfView = PDF_VIEW;

	// handlers

	/**
	 * Custom handler for home
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleHome(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView(homeView);
	}

	/**
	 * Custom handler for config
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleConfig(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView(configView);
	}

	/**
	 * Custom handler for countries main paged list
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleMain(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder = (RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (null == listHolder) {
			listHolder = new RefreshablePagedListHolder();
			listHolder.setSourceProvider(new CountriesProvider());
			listHolder.setFilter(new CountriesFilter());
			request.getSession(true).setAttribute(COUNTRIES_ATTR, listHolder);
		}
		BindException ex = BindUtils.bind(request, listHolder, "countries");
		listHolder.setLocale(RequestContextUtils.getLocale(request));
		boolean forceRefresh = request.getParameter("forceRefresh") != null;
		listHolder.refresh(forceRefresh);
		return new ModelAndView(mainView, ex.getModel());
	}

	/**
	 * Custom handler for countries detail page
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Locale locale = RequestContextUtils.getLocale(request);
		ICountry country = daoCountry.getCountry(request.getParameter("code"), locale);
		return new ModelAndView(detailView, "country", country);
	}

	/**
	 * Custom handler for countries Excel document
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleExcel(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder = (RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (null == listHolder) {
			throw new ServletException("No countries list found in session");
		}
		return new ModelAndView(excelView, "countries", listHolder);
	}

	/**
	 * Custom handler for countries PDF document
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handlePdf(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder = (RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (null == listHolder) {
			throw new ServletException("No countries list found in session");
		}
		return new ModelAndView(pdfView, "countries", listHolder);
	}

	/**
	 * Custom handler for copy countries from memory to database
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleCopy(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		boolean copyMade = false;
		if (null != secondDaoCountry) {
			logger.info("A secondDao is configured");
			secondDaoCountry.initBase();
			logger.info("The database is initiallised");
			Locale locs[] = {Locale.US, Locale.FRANCE, Locale.GERMANY};
			for (int i = 0; i < locs.length; i++ ) {
				secondDaoCountry.saveCountries(daoCountry.getAllCountries(locs[i]), locs[i]);
			}
			copyMade = true;
			logger.info("The data is copied");
		}
		else {
			logger.error("No secondDao is configured. You cannot copy in a database.");
		}
		return new ModelAndView("copyView", "copyMade", new Boolean(copyMade));
	}

	// Accessors
	public IDaoCountry getSecondDaoCountry() {
		return secondDaoCountry;
	}

	public void setDaoCountry(IDaoCountry daoCountry) {
		this.daoCountry = daoCountry;
	}

	public void setSecondDaoCountry(IDaoCountry country) {
		secondDaoCountry = country;
	}

	public void setConfigView(String string) {
		configView = string;
	}

	public void setDetailView(String string) {
		detailView = string;
	}

	public void setExcelView(String string) {
		excelView = string;
	}

	public void setHomeView(String string) {
		homeView = string;
	}

	public void setMainView(String string) {
		mainView = string;
	}

	public void setPdfView(String string) {
		pdfView = string;
	}


	// Embedded classes
	private class CountriesProvider implements PagedListSourceProvider {

		public List loadList(Locale loc, Object filter) {
			CountriesFilter cf = (CountriesFilter) filter;
			return daoCountry.getFilteredCountries(cf.getName(), cf.getCode(), loc);
		}
	}


	public static class CountriesFilter {

		private String name;
		private String code;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public boolean equals(Object obj) {
			return (obj instanceof CountriesFilter ? equals((CountriesFilter) obj) : false);
		}

		public boolean equals(CountriesFilter cf) {
			if (cf == this) return true;
			boolean result = (name == null ? cf.name == null : name.equals(cf.name));
			if (result) {
				result = (code == null ? cf.code == null : code.equals(cf.code));
			}
			return result;
		}

		public int hashCode() {
			int hash = 17;
			hash = 37 * hash + (name == null ? 0 : name.hashCode());
			hash = 37 * hash + (code == null ? 0 : code.hashCode());
			return hash;
		}
	}

}
