package org.springframework.web.flow.config;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;

public class FlowConversionService extends DefaultConversionService implements ConversionService {
	public FlowConversionService() {
		super();
		addDefaultConverters();
		addConverter(new TextToTransitionCriteria());
		addConverter(new TextToViewDescriptorCreator());
	}
}
