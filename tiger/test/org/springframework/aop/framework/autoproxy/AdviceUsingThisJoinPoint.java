package org.springframework.aop.framework.autoproxy;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class AdviceUsingThisJoinPoint {

  private String lastEntry = "";	

  public String getLastMethodEntered() {
	  return this.lastEntry;
  }
  
  @Pointcut("execution(* *(..))")
  public void methodExecution() {}
			  
  @Before("methodExecution()")
  public void entryTrace(JoinPoint jp) {
	  this.lastEntry = jp.toString();
  }

}
