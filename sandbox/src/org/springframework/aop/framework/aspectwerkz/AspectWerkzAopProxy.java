package org.springframework.aop.framework.aspectwerkz;

import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;

import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.*;

import org.codehaus.aspectwerkz.proxy.Proxy;
import org.codehaus.aspectwerkz.intercept.Advisable;
import org.codehaus.aspectwerkz.intercept.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rob Harrop
 */
public class AspectWerkzAopProxy implements AopProxy{

    private static final Log log = LogFactory.getLog(AspectWerkzAopProxy.class);

    private AdvisedSupport advised;

	public AspectWerkzAopProxy(AdvisedSupport advised) {
		this.advised = advised;
	}

	public Object getProxy() {
		return getProxy(null);
	}

	public Object getProxy(ClassLoader classLoader) {
		Object proxy = Proxy.newInstance(advised.getTargetSource().getTargetClass(), true, true);
		Advisable advisable = (Advisable) proxy;
		Advisor[] advisors = advised.getAdvisors();

        Class targetClass = advised.getTargetSource().getTargetClass();

		// todo: need to support pointcuts somehow
		// todo: need transparent support for expression-based pointcuts
		String catchAll   = "execution(* " + targetClass.getName() + ".*(..))"; // could be made more specific

		for (int x = 0; x < advisors.length; x++) {
			Advisor advisor = advisors[x];

            processAdvisor(advisor, targetClass, advisable, catchAll);
        }
		advisable.aw_addAdvice(catchAll, new TargetRoutingInterceptor());
		return proxy;
	}

    private void processAdvisor(Advisor advisor, Class targetClass, Advisable advisable, String catchAll) {
        if(advisor instanceof PointcutAdvisor) {
            Pointcut p = ((PointcutAdvisor)advisor).getPointcut();

            if(p.getClassFilter().matches(targetClass) && p.getMethodMatcher() == MethodMatcher.TRUE) {
                log.info("Matches this class and all methods - using catch all expression");
                advisable.aw_addAdvice(catchAll, new MethodInterceptorAdapter((MethodInterceptor) advisor.getAdvice()));
            } else if(p instanceof ExpressionBasedPointcut) {
                log.info("Expression based pointcut - using expression");
            } else {
                throw new UnsupportedOperationException("foo");
            }
        } else {
            log.info("No pointcut - using catch all expression");
            advisable.aw_addAdvice(catchAll, new MethodInterceptorAdapter((MethodInterceptor) advisor.getAdvice()));
        }
    }

    public class MethodInterceptorAdapter implements AroundAdvice {

		private MethodInterceptor interceptor;

		public MethodInterceptorAdapter(MethodInterceptor interceptor) {
			this.interceptor = interceptor;
		}

		public Object invoke(JoinPoint jp) throws Throwable {
			Method m = getMethodFromJoinPoint(jp);

			if(m.getName().startsWith("aw_")) {
				return jp.proceed();
			} else {
				JoinPointDelegator del = new JoinPointDelegator(jp);
				return interceptor.invoke(del);
			}
		}
	}

	public class TargetRoutingInterceptor implements AroundAdvice {

		public Object invoke(JoinPoint jp) throws Throwable {
			MethodRtti rtti = (MethodRtti) jp.getRtti();
			Method proxiedMethod = rtti.getMethod();

			Class superClass = proxiedMethod.getDeclaringClass().getSuperclass();
			try {
				Method m = superClass.getMethod(proxiedMethod.getName(), proxiedMethod.getParameterTypes());
				return m.invoke(advised.getTargetSource().getTarget(), getArgsFromJoinPoint(jp));
			}
			catch (NoSuchMethodException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private Object[] getArgsFromJoinPoint(JoinPoint joinPoint) {
		MethodRtti rtti = (MethodRttiImpl) joinPoint.getRtti();

		return rtti.getParameterValues();
	}

	private Method getMethodFromJoinPoint(JoinPoint joinPoint) {
		// todo: clean up the number of calls to this method
		MethodRtti rtti = (MethodRttiImpl) joinPoint.getRtti();
		Method proxiedMethod = rtti.getMethod();

		Class superClass = proxiedMethod.getDeclaringClass().getSuperclass();
		try {
			return superClass.getMethod(proxiedMethod.getName(), proxiedMethod.getParameterTypes());
		}
		catch (NoSuchMethodException ex) {
			return proxiedMethod;
		}
	}

	public class JoinPointDelegator implements MethodInvocation {

		private JoinPoint jp;

		private Method method;

		private Object[] args;

		public JoinPointDelegator(JoinPoint jp) {
			this.jp = jp;
			this.method = getMethodFromJoinPoint(jp);
			this.args = getArgsFromJoinPoint(jp);
		}

		public Method getMethod() {
			return method;
		}

		public Object[] getArguments() {
			return args;
		}

		public Object proceed() throws Throwable {
			return jp.proceed();
		}

		public Object getThis() {
			return jp.getRtti().getThis();
		}

		public AccessibleObject getStaticPart() {
			return method;
		}
	}
}
