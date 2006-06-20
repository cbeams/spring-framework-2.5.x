/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.scheduling.quartz;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerListener;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import org.springframework.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.scheduling.TestMethodInvokingTask;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @author Rob Harrop
 * @since 20.02.2004
 */
public class QuartzSupportTests extends TestCase {

	public void testSchedulerFactoryBean() throws Exception {
		doTestSchedulerFactoryBean(false);
	}

	public void testSchedulerFactoryBeanWithExplicitJobDetail() throws Exception {
		doTestSchedulerFactoryBean(true);
	}

	private void doTestSchedulerFactoryBean(boolean explicitJobDetail) throws Exception {
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
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
				return scheduler;
			}
		};
		Map schedulerContext = new HashMap();
		schedulerContext.put("otherTestBean", tb);
		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContext);
		if (explicitJobDetail) {
			schedulerFactoryBean.setJobDetails(new JobDetail[] {jobDetail0});
		}
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger0, trigger1});
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	public void testSchedulerFactoryBeanWithExistingJobs() throws Exception {
		doTestSchedulerFactoryBeanWithExistingJobs(false);
	}

	public void testSchedulerFactoryBeanWithOverwriteExistingJobs() throws Exception {
		doTestSchedulerFactoryBeanWithExistingJobs(true);
	}

	private void doTestSchedulerFactoryBeanWithExistingJobs(boolean overwrite) throws Exception {
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
		scheduler.getTrigger("myTrigger0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(new SimpleTrigger());
		if (overwrite) {
			scheduler.addJob(jobDetail1, true);
			schedulerControl.setVoidCallable();
			scheduler.rescheduleJob("myTrigger1", Scheduler.DEFAULT_GROUP, trigger1);
			schedulerControl.setReturnValue(new Date());
		}
		else {
			scheduler.getJobDetail("myJob0", Scheduler.DEFAULT_GROUP);
			schedulerControl.setReturnValue(null);
		}
		scheduler.addJob(jobDetail0, true);
		schedulerControl.setVoidCallable();
		scheduler.scheduleJob(trigger0);
		schedulerControl.setReturnValue(new Date());
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
				return scheduler;
			}
		};
		Map schedulerContext = new HashMap();
		schedulerContext.put("otherTestBean", tb);
		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContext);
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger0, trigger1});
		if (overwrite) {
			schedulerFactoryBean.setOverwriteExistingJobs(true);
		}
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	public void testSchedulerFactoryBeanWithExistingJobsAndRaceCondition() throws Exception {
		doTestSchedulerFactoryBeanWithExistingJobsAndRaceCondition(false);
	}

	public void testSchedulerFactoryBeanWithOverwriteExistingJobsAndRaceCondition() throws Exception {
		doTestSchedulerFactoryBeanWithExistingJobsAndRaceCondition(true);
	}

	private void doTestSchedulerFactoryBeanWithExistingJobsAndRaceCondition(boolean overwrite) throws Exception {
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
		scheduler.getTrigger("myTrigger0", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(null);
		scheduler.getTrigger("myTrigger1", Scheduler.DEFAULT_GROUP);
		schedulerControl.setReturnValue(new SimpleTrigger());
		if (overwrite) {
			scheduler.addJob(jobDetail1, true);
			schedulerControl.setVoidCallable();
			scheduler.rescheduleJob("myTrigger1", Scheduler.DEFAULT_GROUP, trigger1);
			schedulerControl.setReturnValue(new Date());
		}
		else {
			scheduler.getJobDetail("myJob0", Scheduler.DEFAULT_GROUP);
			schedulerControl.setReturnValue(null);
		}
		scheduler.addJob(jobDetail0, true);
		schedulerControl.setVoidCallable();
		scheduler.scheduleJob(trigger0);
		schedulerControl.setThrowable(new ObjectAlreadyExistsException(""));
		if (overwrite) {
			scheduler.rescheduleJob("myTrigger0", Scheduler.DEFAULT_GROUP, trigger0);
			schedulerControl.setReturnValue(new Date());
		}
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
				return scheduler;
			}
		};
		Map schedulerContext = new HashMap();
		schedulerContext.put("otherTestBean", tb);
		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContext);
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger0, trigger1});
		if (overwrite) {
			schedulerFactoryBean.setOverwriteExistingJobs(true);
		}
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	public void testSchedulerFactoryBeanWithListeners() throws Exception {
		MockControl schedulerControl = MockControl.createControl(Scheduler.class);
		final Scheduler scheduler = (Scheduler) schedulerControl.getMock();

		SchedulerListener schedulerListener = new TestSchedulerListener();
		JobListener globalJobListener = new TestJobListener();
		JobListener jobListener = new TestJobListener();
		TriggerListener globalTriggerListener = new TestTriggerListener();
		TriggerListener triggerListener = new TestTriggerListener();

		scheduler.addSchedulerListener(schedulerListener);
		schedulerControl.setVoidCallable();
		scheduler.addGlobalJobListener(globalJobListener);
		schedulerControl.setVoidCallable();
		scheduler.addJobListener(jobListener);
		schedulerControl.setVoidCallable();
		scheduler.addGlobalTriggerListener(globalTriggerListener);
		schedulerControl.setVoidCallable();
		scheduler.addTriggerListener(triggerListener);
		schedulerControl.setVoidCallable();
		scheduler.start();
		schedulerControl.setVoidCallable();
		scheduler.shutdown(false);
		schedulerControl.setVoidCallable();
		schedulerControl.replay();

		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean() {
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
				return scheduler;
			}
		};
		schedulerFactoryBean.setSchedulerListeners(new SchedulerListener[]{schedulerListener});
		schedulerFactoryBean.setGlobalJobListeners(new JobListener[]{globalJobListener});
		schedulerFactoryBean.setJobListeners(new JobListener[]{jobListener});
		schedulerFactoryBean.setGlobalTriggerListeners(new TriggerListener[]{globalTriggerListener});
		schedulerFactoryBean.setTriggerListeners(new TriggerListener[]{triggerListener});
		try {
			schedulerFactoryBean.afterPropertiesSet();
		}
		finally {
			schedulerFactoryBean.destroy();
		}

		schedulerControl.verify();
	}

	/*public void testMethodInvocationWithConcurrency() throws Exception {
		methodInvokingConcurrency(true);
	}*/
	
	// We can't test both since Quartz somehow seems to keep things in memory
	// enable both and one of them will fail (order doesn't matter).
	/*public void testMethodInvocationWithoutConcurrency() throws Exception {
		methodInvokingConcurrency(false);
	}*/

	private void methodInvokingConcurrency(boolean concurrent) throws Exception {
		// Test the concurrency flag.
		// Method invoking job with two triggers.
		// If the concurrent flag is false, the triggers are NOT allowed
		// to interfere with each other.

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
		schedulerFactoryBean.setJobDetails(new JobDetail[] {jobDetail1});
		schedulerFactoryBean.setTriggers(new Trigger[] {trigger1, trigger0});
		schedulerFactoryBean.afterPropertiesSet();
		
		// ok scheduler is set up... let's wait for like 4 seconds
		try {
			Thread.sleep(4000);
		}
		catch (InterruptedException ex) {
			// fall through
		}

		if (concurrent) {
			assertEquals(2, task1.counter);
			task1.stop();
			// we're done, both jobs have ran, let's call it a day
			return;
		}
		else {
			assertEquals(1, task1.counter);
			task1.stop();
			// we need to check whether or not the test succeed with non-concurrent jobs
		}

		try {
			Thread.sleep(4000);
		}
		catch (InterruptedException ex) {
			// fall through
		}

		task1.stop();
		assertEquals(2, task1.counter);
		
		// Although we're destroying the scheduler, it does seem to keep things in memory:
		// When executing both tests (concurrent and non-concurrent), the second test always
		// fails.
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
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
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
			protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) {
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

	public void testMethodInvokingJobDetailFactoryBeanWithListenerNames() throws Exception {
		TestMethodInvokingTask task = new TestMethodInvokingTask();
		MethodInvokingJobDetailFactoryBean mijdfb = new MethodInvokingJobDetailFactoryBean();
		String[] names = new String[] {"test1", "test2"};
		mijdfb.setName("myJob1");
		mijdfb.setGroup(Scheduler.DEFAULT_GROUP);
		mijdfb.setTargetObject(task);
		mijdfb.setTargetMethod("doSomething");
		mijdfb.setJobListenerNames(names);
		mijdfb.afterPropertiesSet();
		JobDetail jobDetail = (JobDetail) mijdfb.getObject();
		List result = Arrays.asList(jobDetail.getJobListenerNames());
		assertEquals(Arrays.asList(names), result);
	}

	public void testJobDetailBeanWithListenerNames() {
		JobDetailBean jobDetail = new JobDetailBean();
		String[] names = new String[] {"test1", "test2"};
		jobDetail.setJobListenerNames(names);
		List result = Arrays.asList(jobDetail.getJobListenerNames());
		assertEquals(Arrays.asList(names), result);
	}

	public void testCronTriggerBeanWithListenerNames() {
		CronTriggerBean trigger = new CronTriggerBean();
		String[] names = new String[] {"test1", "test2"};
		trigger.setTriggerListenerNames(names);
		List result = Arrays.asList(trigger.getTriggerListenerNames());
		assertEquals(Arrays.asList(names), result);
	}

	public void testSimpleTriggerBeanWithListenerNames() {
		SimpleTriggerBean trigger = new SimpleTriggerBean();
		String[] names = new String[] {"test1", "test2"};
		trigger.setTriggerListenerNames(names);
		List result = Arrays.asList(trigger.getTriggerListenerNames());
		assertEquals(Arrays.asList(names), result);
	}

	/**
	 * Tests the creation of multiple schedulers (SPR-772)
	 */
	public void testMultipleSchedulers() throws Exception {
		ClassPathXmlApplicationContext ctx =
				new ClassPathXmlApplicationContext("/org/springframework/scheduling/quartz/multipleSchedulers.xml");

		try {
			Scheduler scheduler1 = (Scheduler) ctx.getBean("scheduler1");
			Scheduler scheduler2 = (Scheduler) ctx.getBean("scheduler2");

			assertNotSame(scheduler1, scheduler2);
			assertFalse(scheduler1.getSchedulerName().equals(scheduler2.getSchedulerName()));
		}
		finally {
			ctx.close();
		}
	}

	public void testWithTwoAnonymousMethodInvokingJobDetailFactoryBeans() throws InterruptedException {
		ClassPathXmlApplicationContext ctx =
				new ClassPathXmlApplicationContext("/org/springframework/scheduling/quartz/multipleAnonymousMethodInvokingJobDetailFB.xml");
		Thread.sleep(3000);
		try {
			QuartzTestBean exportService = (QuartzTestBean) ctx.getBean("exportService");
			QuartzTestBean importService = (QuartzTestBean) ctx.getBean("importService");

			assertEquals("doImport called exportService", 0, exportService.getImportCount());
			assertEquals("doExport not called on exportService", 2, exportService.getExportCount());
			assertEquals("doImport not called on importService", 2, importService.getImportCount());
			assertEquals("doExport called on importService", 0, importService.getExportCount());
		}
		finally {
			ctx.close();
		}
	}


	private static class TestSchedulerListener implements SchedulerListener {

		public void jobScheduled(Trigger trigger) {
		}

		public void jobUnscheduled(String triggerName, String triggerGroup) {
		}

		public void triggerFinalized(Trigger trigger) {
		}

		public void triggersPaused(String triggerName, String triggerGroup) {
		}

		public void triggersResumed(String triggerName, String triggerGroup) {
		}

		public void jobsPaused(String jobName, String jobGroup) {
		}

		public void jobsResumed(String jobName, String jobGroup) {
		}

		public void schedulerError(String msg, SchedulerException cause) {
		}

		public void schedulerShutdown() {
		}
	}


	private static class TestJobListener implements JobListener {

		public String getName() {
			return null;
		}

		public void jobToBeExecuted(JobExecutionContext context) {
		}

		public void jobExecutionVetoed(JobExecutionContext context) {
		}

		public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		}
	}


	private static class TestTriggerListener implements TriggerListener {

		public String getName() {
			return null;
		}

		public void triggerFired(Trigger trigger, JobExecutionContext context) {
		}

		public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
			return false;
		}

		public void triggerMisfired(Trigger trigger) {
		}

		public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {
		}
	}

}
