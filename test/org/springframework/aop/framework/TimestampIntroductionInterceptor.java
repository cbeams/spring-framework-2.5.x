package org.springframework.aop.framework;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;

public class TimestampIntroductionInterceptor extends DelegatingIntroductionInterceptor
	implements TimeStamped {

	private long ts;

	public TimestampIntroductionInterceptor() {
	}

	public TimestampIntroductionInterceptor(long ts) {
		this.ts = ts;
	}
	
	public void setTime(long ts) {
		this.ts = ts;
	}

	public long getTimeStamp() {
		return ts;
	}

}
