/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.SchedulerContext;

import org.springframework.beans.TestBean;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.scheduling.timer.MethodInvokingTimerTaskFactoryBean;
import org.springframework.scheduling.timer.ScheduledTimerTask;
import org.springframework.scheduling.timer.TimerFactoryBean;

/**
 * @author Juergen Hoeller
 * @since 20.02.2004
 */
public class SchedulingTestSuite extends TestCase {

	public void testTimerFactoryBean() throws Exception {
		final TestTimerTask timerTask0 = new TestTimerTask();
		TestMethodInvokingTask task1 = new TestMethodInvokingTask();
		MethodInvokingTimerTaskFactoryBean mittfb = new MethodInvokingTimerTaskFactoryBean();
		mittfb.setTargetObject(task1);
		mittfb.setTargetMethod("doSomething");
		mittfb.afterPropertiesSet();
		final TimerTask timerTask1 = (TimerTask) mittfb.getObject();

		ScheduledTimerTask[] tasks = new ScheduledTimerTask[2];
		tasks[0] = new ScheduledTimerTask(timerTask0, 0, 10, false);
		tasks[1] = new ScheduledTimerTask(timerTask1, 10, 20, true);

		final List success = new ArrayList(3);
		final Timer timer = new Timer(true) {
			public void schedule(TimerTask task, long delay, long period) {
				if (task == timerTask0 && delay == 0 && period == 10) {
					success.add(Boolean.TRUE);
				}
			}
			public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
				if (task == timerTask1 && delay == 10 && period == 20) {
					success.add(Boolean.TRUE);
				}
			}
			public void cancel() {
				success.add(Boolean.TRUE);
			}
		};

		TimerFactoryBean timerFactoryBean = new TimerFactoryBean() {
			protected Timer createTimer(boolean daemon) {
				return timer;
			}
		};
		try {
			timerFactoryBean.setScheduledTimerTasks(tasks);
			timerFactoryBean.afterPropertiesSet();
			assertTrue(timerFactoryBean.getObject() instanceof Timer);
			timerTask0.run();
			timerTask1.run();
		}
		finally {
			timerFactoryBean.destroy();
		}

		assertTrue("Correct Timer invocations", success.size() == 3);
		assertTrue("TimerTask0 works", timerTask0.counter == 1);
		assertTrue("TimerTask1 works", task1.counter == 1);
	}

	public void testSchedulerFactoryBean() throws Exception {
		TestBean tb = new TestBean("tb", 99);
		JobDetailBean jobDetail0 = new JobDetailBean();
		jobDetail0.setJobClass(Job.class);
		jobDetail0.setBeanName("myJob0");
		Map jobData = new HashMap();
		jobData.put("testBean", tb);
		jobDetail0.setJobDataAsMap(jobData);
		jobDetail0.afterPropertiesSet();
		assertEquals(tb, jobDetail0.getJobDataMap().get("testBean"));

		CronTriggerBean trigger0 = new CronTriggerBean();
		trigger0.setBeanName("myTrigger0");
		trigger0.setJobDetail(jobDetail0);
		trigger0.setCronExpression("0/1 * * * * ?");
		trigger0.afterPropertiesSet();

		TestMethodInvokingTask task1 = new TestMethodInvokingTask();
		MethodInvokingJobDetailFactoryBean mijdfb = new MethodInvokingJobDetailFactoryBean();
		mijdfb.setBeanName("myJob1");
		mijdfb.setTargetObject(task1);
		mijdfb.setTargetMethod("doSomething");
		mijdfb.afterPropertiesSet();
		JobDetail jobDetail1 = (JobDetail) mijdfb.getObject();

		SimpleTriggerBean trigger1 = new SimpleTriggerBean();
		trigger1.setBeanName("myTrigger1");
		trigger1.setJobDetail(jobDetail1);
		trigger1.setStartDelay(0);
		trigger1.setRepeatInterval(20);
		trigger1.afterPropertiesSet();

		MockControl schedulerControl = MockControl.createControl(Scheduler.class);
		final Scheduler scheduler = (Scheduler) schedulerControl.getMock();
		scheduler.getContext();
		schedulerControl.setReturnValue(new SchedulerContext());
		scheduler.getJobDetail("myJob0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getJobDetail("myJob1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.addJob(jobDetail0, true);
		schedulerControl.setVoidCallable();
		scheduler.scheduleJob(trigger0);
		schedulerControl.setReturnValue(new Date());
		scheduler.addJob(jobDetail1, true);
		schedulerControl.setVoidCallable();
		scheduler.scheduleJob(trigger1);
		schedulerControl.setReturnValue(new Date());
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName)
					throws SchedulerException {
				return scheduler;
			}
		};
		Map schedulerContext = new HashMap();
		schedulerContext.put("otherTestBean", tb);
		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContext);
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger0, trigger1});
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}
	
	public void testMethodInvocationWithConcurrency() throws Exception {
		methodInvokingConcurrency(true);
	}
	
	// we can't test both since Quartz somehow seems to keep things in memory
	// enable both and one of them will fail (order doesn't matter)
	/*public void testMethodInvocationWithoutConcurrency() throws Exception {
		methodInvokingConcurrency(false);
	}*/
	
	public void methodInvokingConcurrency(boolean concurrent) 
	throws Exception {
		
		// test the concurrency flag
		// method invoking job with two triggers
		// if the concurrent flag is false, the triggers are NOT allowed
		// to interfere with eachother

		TestMethodInvokingTask task1 = new TestMethodInvokingTask();
		MethodInvokingJobDetailFactoryBean mijdfb = new MethodInvokingJobDetailFactoryBean();
		// set the concurrency flag!
		mijdfb.setConcurrent(concurrent);
		mijdfb.setBeanName("myJob1");
		mijdfb.setTargetObject(task1);
		mijdfb.setTargetMethod("doWait");
		mijdfb.afterPropertiesSet();
		JobDetail jobDetail1 = (JobDetail) mijdfb.getObject();

		SimpleTriggerBean trigger0 = new SimpleTriggerBean();
		trigger0.setBeanName("myTrigger1");
		trigger0.setJobDetail(jobDetail1);
		trigger0.setStartDelay(0);		
		trigger0.setRepeatInterval(1);
		trigger0.setRepeatCount(1);
		trigger0.afterPropertiesSet();
		
		SimpleTriggerBean trigger1 = new SimpleTriggerBean();
		trigger1.setBeanName("myTrigger1");
		trigger1.setJobDetail(jobDetail1);
		trigger1.setStartDelay(1000L);		
		trigger1.setRepeatInterval(1);
		trigger1.setRepeatCount(1);
		trigger1.afterPropertiesSet();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		
		
		schedulerFactoryBean.setJobDetails(new JobDetail[] { jobDetail1 } );
		schedulerFactoryBean.setTriggers(new Trigger[] { trigger1, trigger0} );		
		schedulerFactoryBean.afterPropertiesSet();
		
		// ok scheduler is set up... let's wait for like 1 seconds
		try {
			Thread.sleep(4000);			
		} catch (InterruptedException e) {
			// fall through
		}
		
		if (concurrent) {
			assertEquals(2, task1.counter);
			task1.stop();
			// we're done, both jobs have ran, let's call it a day
			return;
		} else {			
			assertEquals(1, task1.counter);
			task1.stop();
			// we need to check whether or not the test succeed with non-concurrent jobs
		}		
		
		try {
			Thread.sleep(4000);			
		} catch (InterruptedException e) {
			// fall through
		}
		
		task1.stop();
		
		assertEquals(2, task1.counter);
		
		// although we're destroying the scheduler, it does seem to keep things in memory,
		// when executing both tests (concurrent and non-concurrent), the second test always
		// fails
		schedulerFactoryBean.destroy();
		
	}

	public void testSchedulerFactoryBeanWithPlainQuartzObjects() throws Exception {
		TestBean tb = new TestBean("tb", 99);
		JobDetail jobDetail0 = new JobDetail();
		jobDetail0.setJobClass(Job.class);
		jobDetail0.setName("myJob0");
		jobDetail0.setGroup(Scheduler.DEFAULT_GROUP);
		jobDetail0.getJobDataMap().put("testBean", tb);
		assertEquals(tb, jobDetail0.getJobDataMap().get("testBean"));

		CronTrigger trigger0 = new CronTrigger();
		trigger0.setName("myTrigger0");
		trigger0.setGroup(Scheduler.DEFAULT_GROUP);
		trigger0.setJobName("myJob0");
		trigger0.setJobGroup(Scheduler.DEFAULT_GROUP);
		trigger0.setStartTime(new Date());
		trigger0.setCronExpression("0/1 * * * * ?");

		TestMethodInvokingTask task1 = new TestMethodInvokingTask();
		MethodInvokingJobDetailFactoryBean mijdfb = new MethodInvokingJobDetailFactoryBean();
		mijdfb.setName("myJob1");
		mijdfb.setGroup(Scheduler.DEFAULT_GROUP);
		mijdfb.setTargetObject(task1);
		mijdfb.setTargetMethod("doSomething");
		mijdfb.afterPropertiesSet();
		JobDetail jobDetail1 = (JobDetail) mijdfb.getObject();

		SimpleTrigger trigger1 = new SimpleTrigger();
		trigger1.setName("myTrigger1");
		trigger1.setGroup(Scheduler.DEFAULT_GROUP);
		trigger1.setJobName("myJob1");
		trigger1.setJobGroup(Scheduler.DEFAULT_GROUP);
		trigger1.setStartTime(new Date());
		trigger1.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		trigger1.setRepeatInterval(20);

		MockControl schedulerControl = MockControl.createControl(Scheduler.class);
		final Scheduler scheduler = (Scheduler) schedulerControl.getMock();
		scheduler.getJobDetail("myJob0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getJobDetail("myJob1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.addJob(jobDetail0, true);
		schedulerControl.setVoidCallable();
		scheduler.addJob(jobDetail1, true);
		schedulerControl.setVoidCallable();
		scheduler.scheduleJob(trigger0);
		schedulerControl.setReturnValue(new Date());
		scheduler.scheduleJob(trigger1);
		schedulerControl.setReturnValue(new Date());
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName)
					throws SchedulerException {
				return scheduler;
			}
		};
		schedulerFactoryBean.setJobDetails(new JobDetail[] {jobDetail0, jobDetail1});
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger0, trigger1});
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	public void testSchedulerFactoryBeanWithApplicationContext() throws Exception {
		TestBean tb = new TestBean("tb", 99);
		StaticApplicationContext ac = new StaticApplicationContext();

		MockControl schedulerControl = MockControl.createControl(Scheduler.class);
		final Scheduler scheduler = (Scheduler) schedulerControl.getMock();
		SchedulerContext schedulerContext = new SchedulerContext();
		scheduler.getContext();
		schedulerControl.setReturnValue(schedulerContext, 4);
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName)
					throws SchedulerException {
				return scheduler;
			}
		};
		Map schedulerContextMap = new HashMap();
		schedulerContextMap.put("testBean", tb);
		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContextMap);
		schedulerFactoryBean.setApplicationContext(ac);
		schedulerFactoryBean.setApplicationContextSchedulerContextKey("appCtx");
		try {
			schedulerFactoryBean.afterPropertiesSet();
			Scheduler returnedScheduler = (Scheduler) schedulerFactoryBean.getObject();
			assertEquals(tb, returnedScheduler.getContext().get("testBean"));
			assertEquals(ac, returnedScheduler.getContext().get("appCtx"));
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	public void testJobDetailBeanWithApplicationContext() throws Exception {
		TestBean tb = new TestBean("tb", 99);
		StaticApplicationContext ac = new StaticApplicationContext();

		JobDetailBean jobDetail = new JobDetailBean();
		jobDetail.setJobClass(Job.class);
		jobDetail.setBeanName("myJob0");
		Map jobData = new HashMap();
		jobData.put("testBean", tb);
		jobDetail.setJobDataAsMap(jobData);
		jobDetail.setApplicationContext(ac);
		jobDetail.setApplicationContextJobDataKey("appCtx");
		jobDetail.afterPropertiesSet();

		assertEquals(tb, jobDetail.getJobDataMap().get("testBean"));
		assertEquals(ac, jobDetail.getJobDataMap().get("appCtx"));
	}


	private static class TestTimerTask extends TimerTask {

		private int counter = 0;

		public void run() {
			counter++;
		}
	}


	public static class TestMethodInvokingTask {

		private int counter = 0;
		
		private Object lock = new Object();
		
		public void doSomething() {
			counter++;
		}
		
		public void doWait() {
			counter++;
			// wait until stop is called
			synchronized (lock) {
				try {
					lock.wait();
				} 
				catch (InterruptedException e) {
					// fall through
				}
			}
		}
		
		void stop() {
			synchronized(lock) {
				lock.notify();
			}
		}
	}

}
