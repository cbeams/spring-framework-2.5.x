package org.springframework.samples.countries.web;

import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.support.PagedListSourceProvider;
import org.springframework.beans.support.RefreshablePagedListHolder;
import org.springframework.samples.countries.Country;
import org.springframework.samples.countries.CountryService;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public class CountriesController extends MultiActionController {

	private static final String COUNTRIES_ATTR = "countries";
	private static final String HOME_VIEW = "homeView";
	private static final String CONFIG_VIEW = "configView";
	private static final String MAIN_VIEW = "countries_mainView";
	private static final String DETAIL_VIEW = "countries_detailView";
	private static final String EXCEL_VIEW = "countries_excelView";
	private static final String PDF_VIEW = "countries_pdfView";

	private CountryService countryService;

	private String homeView = HOME_VIEW;
	private String configView = CONFIG_VIEW;
	private String mainView = MAIN_VIEW;
	private String detailView = DETAIL_VIEW;
	private String excelView = EXCEL_VIEW;
	private String pdfView = PDF_VIEW;


	public void setCountryService(CountryService countryService) {
		this.countryService = countryService;
	}

	public void setConfigView(String view) {
		this.configView = view;
	}

	public void setDetailView(String view) {
		this.detailView = view;
	}

	public void setExcelView(String view) {
		this.excelView = view;
	}

	public void setHomeView(String view) {
		this.homeView = view;
	}

	public void setMainView(String view) {
		this.mainView = view;
	}

	public void setPdfView(String view) {
		this.pdfView = view;
	}


	/**
	 * Custom handler for home.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleHome(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView(homeView);
	}

	/**
	 * Custom handler for config.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleConfig(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		return new ModelAndView(configView);
	}

	/**
	 * Custom handler for countries main paged list.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleMain(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder =
				(RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (listHolder == null) {
			listHolder = new RefreshablePagedListHolder();
			listHolder.setSourceProvider(new CountriesProvider());
			listHolder.setFilter(new CountriesFilter());
			request.getSession(true).setAttribute(COUNTRIES_ATTR, listHolder);
		}

		ServletRequestDataBinder binder = new ServletRequestDataBinder(listHolder, "countries");
		binder.bind(request);

		listHolder.setLocale(RequestContextUtils.getLocale(request));
		boolean forceRefresh = request.getParameter("forceRefresh") != null;
		listHolder.refresh(forceRefresh);

		return new ModelAndView(this.mainView, binder.getBindingResult().getModel());
	}

	/**
	 * Custom handler for countries detail page.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Locale locale = RequestContextUtils.getLocale(request);
		Country country = this.countryService.getCountry(request.getParameter("code"), locale);
		return new ModelAndView(this.detailView, "country", country);
	}

	/**
	 * Custom handler for countries Excel document.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handleExcel(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder =
				(RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (listHolder == null) {
			throw new ServletException("No countries list found in session");
		}
		return new ModelAndView(this.excelView, "countries", listHolder);
	}

	/**
	 * Custom handler for countries PDF document.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render the response
	 */
	public ModelAndView handlePdf(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		RefreshablePagedListHolder listHolder =
				(RefreshablePagedListHolder) request.getSession(true).getAttribute(COUNTRIES_ATTR);
		if (listHolder == null) {
			throw new ServletException("No countries list found in session");
		}
		return new ModelAndView(this.pdfView, "countries", listHolder);
	}


	private class CountriesProvider implements PagedListSourceProvider {

		public List loadList(Locale loc, Object filter) {
			CountriesFilter cf = (CountriesFilter) filter;
			return countryService.getFilteredCountries(cf.getName(), cf.getCode(), loc);
		}
	}

}
