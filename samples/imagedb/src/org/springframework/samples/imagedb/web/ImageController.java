package org.springframework.samples.imagedb.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.samples.imagedb.ImageDatabase;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * MultiActionController for the image list/upload UI.
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class ImageController extends MultiActionController {

	private ImageDatabase imageDatabase;

	public void setImageDatabase(ImageDatabase imageDatabase) {
		this.imageDatabase = imageDatabase;
	}

	public ModelAndView showImageList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ModelAndView("imageList", "images", this.imageDatabase.getImages());
	}

	public ModelAndView streamImageContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.imageDatabase.streamImage(request.getParameter("name"), response.getOutputStream());
		return null;
	}

	public ModelAndView processImageUpload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile = multipartRequest.getFile("image");
		this.imageDatabase.storeImage(name, multipartFile.getInputStream(), (int) multipartFile.getSize(), description);
		return new ModelAndView(new RedirectView("imageList"));
	}

	public ModelAndView clearDatabase(HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.imageDatabase.clearDatabase();
		return new ModelAndView(new RedirectView("imageList"));
	}

}
