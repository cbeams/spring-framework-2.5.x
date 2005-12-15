package org.springframework.transaction.aspectj;

import java.lang.reflect.Method;

import junit.framework.AssertionFailedError;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.CallCountingTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * 
 * 
 * @author Rod Johnson
 *
 */
public class TransactionAspectTests extends AbstractDependencyInjectionSpringContextTests {
	
	private TransactionAspectSupport transactionAspect;
	
	private CallCountingTransactionManager txManager;
	
	private TransactionalAnnotationOnlyOnClassWithNoInterface annotationOnlyOnClassWithNoInterface;
	
	private MethodAnnotationOnClassWithNoInterface methodAnnotationOnly = new MethodAnnotationOnClassWithNoInterface();
	
	public void setAnnotationOnlyOnClassWithNoInterface(
			TransactionalAnnotationOnlyOnClassWithNoInterface annotationOnlyOnClassWithNoInterface) {
		this.annotationOnlyOnClassWithNoInterface = annotationOnlyOnClassWithNoInterface;
	}

	public TransactionAspectSupport getTransactionAspect() {
		return transactionAspect;
	}

	public void setTransactionAspect(TransactionAspectSupport transactionAspect) {
		this.transactionAspect = transactionAspect;
		this.txManager = (CallCountingTransactionManager) transactionAspect.getTransactionManager();
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/transaction/aspectj/txtests.xml" };
	}
	
	private interface TransactionOperationCallback {
		Object performTransactionalOperation() throws Throwable;
	}
	
	public void testCommitOnAnnotatedClass() throws Throwable {
		txManager.clear();
		assertEquals(0, txManager.begun);
		annotationOnlyOnClassWithNoInterface.echo(null);
		assertEquals(1, txManager.commits);
	}
	

	
	public void testCommitOnAnnotatedMethod() throws Throwable {
		txManager.clear();
		assertEquals(0, txManager.begun);
		methodAnnotationOnly.echo(null);
		assertEquals(1, txManager.commits);
	}
	
	public static class NotTransactional {
		public void noop() {
		}
	}
	
	public void testNotTransactional() throws Throwable {
		txManager.clear();
		assertEquals(0, txManager.begun);
		new NotTransactional().noop();
		assertEquals(0, txManager.begun);
	}
	
	
	public void testDefaultCommitOnAnnotatedClass() throws Throwable {
		testRollback(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return annotationOnlyOnClassWithNoInterface.echo(new Exception());
			}
		}, false);
	}
	
	public void testDefaultRollbackOnAnnotatedClass() throws Throwable {
		testRollback(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return annotationOnlyOnClassWithNoInterface.echo(new RuntimeException());
			}
		}, true);
	}
	
	
	public static class SubclassOfClassWithTransactionalAnnotation extends TransactionalAnnotationOnlyOnClassWithNoInterface {
	}
	
	public void testDefaultCommitOnSubclassOfAnnotatedClass() throws Throwable {
		testRollback(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return new SubclassOfClassWithTransactionalAnnotation().echo(new Exception());
			}
		}, false);
	}
	
	public static class SubclassOfClassWithTransactionalMethodAnnotation extends MethodAnnotationOnClassWithNoInterface {
	}
	
	public void testDefaultCommitOnSubclassOfClassWithTransactionalMethodAnnotated() throws Throwable {
		testRollback(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return new SubclassOfClassWithTransactionalMethodAnnotation().echo(new Exception());
			}
		}, false);
	}
	
	public static class ImplementsAnnotatedInterface implements ITransactional {
		public Object echo(Throwable t) throws Throwable {
			if (t != null) {
				throw t;
			}
			return t;
		}
	}
	
	public void testDefaultCommitOnImplementationOfAnnotatedInterface() throws Throwable {
//		testRollback(new TransactionOperationCallback() {
//			public Object performTransactionalOperation() throws Throwable {
//				return new ImplementsAnnotatedInterface().echo(new Exception());
//			}
//		}, false);
		
		final Exception ex = new Exception();
		testNotTransactional(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return new ImplementsAnnotatedInterface().echo(ex);
			}
		}, ex);
	}
	
	/**
	 * Note: resolution does not occur. Thus we can't make a class transactional if
	 * it implements a transactionally annotated interface. This behaviour could only
	 * be changed in AbstractFallbackTransactionAttributeSource in Spring proper.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public void testDoesNotResolveTxAnnotationOnMethodFromClassImplementingAnnotatedInterface() throws SecurityException, NoSuchMethodException {
		AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
		Method m = ImplementsAnnotatedInterface.class.getMethod("echo", Throwable.class);
		TransactionAttribute ta = atas.getTransactionAttribute(m, ImplementsAnnotatedInterface.class);
		assertNull(ta);
	}
	
	
	public void testDefaultRollbackOnImplementationOfAnnotatedInterface() throws Throwable {
//		testRollback(new TransactionOperationCallback() {
//			public Object performTransactionalOperation() throws Throwable {
//				return new ImplementsAnnotatedInterface().echo(new RuntimeException());
//			}
//		}, true);
		
		final Exception rollbackProvokingException = new RuntimeException();
		testNotTransactional(new TransactionOperationCallback() {
			public Object performTransactionalOperation() throws Throwable {
				return new ImplementsAnnotatedInterface().echo(rollbackProvokingException);
			}
		}, rollbackProvokingException);
	}

	
	protected void testRollback(TransactionOperationCallback toc, boolean rollback) throws Throwable {
		txManager.clear();
		assertEquals(0, txManager.begun);
		try {
			toc.performTransactionalOperation();
			assertEquals(1, txManager.commits);
		}
		catch (Throwable caught) {
			if (caught instanceof AssertionFailedError) {
				return;
			}
		}
		
		if (rollback) {
			assertEquals(1, txManager.rollbacks);
		}
		assertEquals(1, txManager.begun);
	}
	
	protected void testNotTransactional(TransactionOperationCallback toc, Throwable expected) throws Throwable {
		txManager.clear();
		assertEquals(0, txManager.begun);
		try {
			toc.performTransactionalOperation();
		}
		catch (Throwable t) {
			if (expected == null) {
				fail("Expected " + expected);
			}
			assertSame(expected, t);
		}
		finally {
			assertEquals(0, txManager.begun);
		}
	}
	

}
