/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view.velocity;

import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationContextException;


/**
 * VelocityLayoutView emulates the functionality offered by Velocity's
 * VelocityLayoutServlet to ease page composition from different templates.
 * <p>
 * The <Code>url</code> property should be set to the template used by the view 
 * and the layout template location set in the <code>layoutUrl</code>
 * property.  A view can override the configured layout location by
 * setting the appropriate key in its own template.
 * <p>
 * When the View is rendered, the VelocityContext is first merged with the
 * screen template (specified by the <code>url</code> property) and then is
 * merged with the layout template to produce the final output. 
 * @author Darren Davison
 * @since 1.2
 */
public class VelocityLayoutView extends VelocityToolboxView {
    
    public static final String DEFAULT_LAYOUT_KEY = "layout";
    
    public static final String DEFAULT_LAYOUT_URL = "layout.vm";

    public static final String DEFAULT_SCREEN_CONTENT_KEY = "screen_content";
    
    private String layoutKey = DEFAULT_LAYOUT_KEY;
    
    private String layoutUrl = DEFAULT_LAYOUT_URL; 
    
    private String screenContentKey = DEFAULT_SCREEN_CONTENT_KEY;

    private Template layoutTemplate;

    private static ThreadLocal overrideLayout = new ThreadLocal();
    
    /**
     * Overrides VelocityView.checkTemplate() to additionally check that
     * both the layout template and the screen content template can be loaded.
     * Note that during rendering of the screen content, the layout template
     * can be changed which may invalidate any early checking done here. 
     * @see org.springframework.web.servlet.view.velocity.VelocityView#checkTemplate()
     */
    protected void checkTemplate() throws ApplicationContextException {
        try {
            // Check that we can get the template, even if we might subsequently get it again.
            this.layoutTemplate = getTemplate(this.layoutUrl);
        }
        catch (ResourceNotFoundException ex) {
            throw new ApplicationContextException("Cannot find Velocity template for URL [" + layoutUrl +
                "]: Did you specify the correct resource loader path?", ex);
        }
        catch (Exception ex) {
            throw new ApplicationContextException(
                    "Could not load Velocity template for URL [" + layoutUrl + "]", ex);
        }
        
        super.checkTemplate();
    }
    
    /**
     * Overrides the normal rendering process in order to pre-process the Context,
     * merging it with the screen template into a single value (identified by the
     * value of screenContentKey).  The layout template is then merged with the 
     * modified Context in the super class.
     * @see org.springframework.web.servlet.view.velocity.VelocityView#doRender()
     */
    protected void doRender(Context context, HttpServletResponse response) throws Exception {
        renderScreenContent(context);
        super.doRender(context, response);
    }

    /**
     * resulting context contains any mappings from render, plus screen content
     * @param velocityContext
     * @throws Exception
     */
    private void renderScreenContent(Context velocityContext) throws Exception {
        logger.debug("rendering screen content with template [" + getUrl() + "]");
        StringWriter sw = new StringWriter();
        Template screenContentTemplate = getVelocityEngine().getTemplate(getUrl());
        screenContentTemplate.merge(velocityContext, sw);

        // velocity context now includes any mappings that
        // were defined--via #set--in screen content template
        // the screen template can overrule the layout
        // by doing #set( $layout = "MyLayout.vm" )
        String overrideLayoutUrl = (String) velocityContext.get(layoutKey);
        if (overrideLayoutUrl != null) {
            logger.debug("layout has been set by screen content template to [" + overrideLayoutUrl + "]");
            overrideLayout.set(overrideLayoutUrl);
        }

        // Put rendered content into context
        velocityContext.put(screenContentKey, sw.toString());
        
    }

    /**
     * Return the layoutTemplate and not the screen content template.
     * @see org.springframework.web.servlet.view.velocity.VelocityView#getTemplate()
     */
    protected Template getTemplate() throws Exception {
        String _url = (String) overrideLayout.get();
        return getTemplate(_url == null ? this.layoutUrl : _url);
    }
    
    /** 
     * Sets the layout template to use.  Defaults to "layout.vm"
     * @param layoutUrl the template location relative to the template 
     * loading root
     */
    public void setLayoutUrl(String layoutUrl) {
        this.layoutUrl = layoutUrl;
    }
     
    /**
     * The context key used to specify an alternate layout 
     * to be used instead of the default layout.  Screen content templates
     * can override the layout template that they wish to be wrapped with
     * by setting this value in the template, for example; 
     * <code>#set($layout="myPreferredLayout.vm")</code> 
     * <p>
     * The default key is "layout" as highlighted above.
     * @param layoutKey the name of the key you wish to use in your
     * screen content templates to override the layout template.
     */ 
    public void setLayoutKey(String layoutKey) {
        this.layoutKey = layoutKey;
    }
    
    /**
     * Sets the name of the context key that will hold the content of 
     * the screen within the layout template. This key must be present 
     * in the layout template for the current screen to be rendered.
     * Defaults to "screen_content" (accessed in VTL as $screen_content)
     * @param screenContentKey the name of the screen content key to use
     */
    public void setScreenContentKey(String screenContentKey) {
        this.screenContentKey = screenContentKey;
    }
    
}
