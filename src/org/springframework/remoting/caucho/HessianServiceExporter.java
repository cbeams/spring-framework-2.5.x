package org.springframework.remoting.caucho;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.server.HessianSkeleton;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.remoting.support.RemoteExporter;

/**
 * Web controller that exports the specified service bean as Hessian service
 * endpoint, accessible via a Hessian proxy.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>Note: Hessian services exported with this class can be accessed by
 * any Hessian client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see HessianProxyFactoryBean
 */
public class HessianServiceExporter extends RemoteExporter implements Controller {

	private HessianSkeleton skeleton;

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.skeleton = new HessianSkeleton(getProxyForService());
	}

	/**
	 * Process the incoming Hessian request and create a Hessian response.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HessianInput in = new HessianInput(request.getInputStream());
		HessianOutput out = new HessianOutput(response.getOutputStream());
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
