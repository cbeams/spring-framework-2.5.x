package org.springframework.samples.fileupload.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.flow.FlowModel;
import org.springframework.web.flow.action.BindAndValidateAction;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

public class ProcessUploadAction extends BindAndValidateAction {

	protected void initBinder(HttpServletRequest request, FlowModel model, ServletRequestDataBinder binder) {
		//to actually be able to convert a multipart object to a byte[]
		//we have to register a custom editor (in this case the
		// ByteArrayMultipartFileEditor)
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		//now Spring knows how to handle multipart objects and convert them
	}

}