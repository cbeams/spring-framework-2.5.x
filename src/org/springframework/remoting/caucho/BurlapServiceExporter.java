package org.springframework.remoting.caucho;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;
import com.caucho.burlap.server.BurlapSkeleton;

import org.springframework.remoting.support.RemoteExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Web controller that exports the specified service bean as Burlap service
 * endpoint, accessible via a Burlap proxy.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>Note: Burlap services exported with this class can be accessed by
 * any Burlap client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see BurlapProxyFactoryBean
 */
public class BurlapServiceExporter extends RemoteExporter implements Controller {

	private BurlapSkeleton skeleton;

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.skeleton = new BurlapSkeleton(getProxyForService());
	}

	/**
	 * Process the incoming Burlap request and create a Burlap response.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		BurlapInput in = new BurlapInput(request.getInputStream());
		BurlapOutput out = new BurlapOutput(response.getOutputStream());
		try {
		  this.skeleton.invoke(in, out);
		}
		catch (Exception ex) {
			throw ex;
		}
		catch (Error ex) {
			throw ex;
		}
		catch (Throwable ex) {
		  throw new ServletException(ex);
		}
		return null;
	}

}
