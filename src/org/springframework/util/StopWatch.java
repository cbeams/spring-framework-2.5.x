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

package org.springframework.util;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple stop watch, allowing for timing of a number of tasks,
 * exposing total running time and running time for each named task.
 * Conceals use of System.currentTimeMillis(), improving the readability
 * of application code and reducing the likelihood of calculation errors.
 *
 * <p>Note that this object is not designed to be threadsafe, and does not
 * use synchronization or threading. Therefore it is safe to invoke it from EJBs.
 *
 * <p>This class is normally used to verify performance during proof-of-concepts
 * and in development, rather than as part of production applications.
 *
 * @author Rod Johnson
 * @since May 2, 2001
 */
public class StopWatch {

	/**
	 * Identifier of this stop watch.
	 * Handy when we have output from multiple stop watches
	 * and need to distinguish between them in log or console output.
	 */
	private String id = "";

	private boolean keepTaskList = true;

	/** Start time of the current task */
	private long startTimeMillis;

	/** List of TaskInfo objects */
	private final List taskList = new LinkedList();

	/** Name of the current task */
	private String currentTaskName;

	/** Is the stop watch currently running? */
	private boolean running;

	private TaskInfo lastTaskInfo;
	
	private int taskCount;

	/** Total running time */
	private long totalTimeMillis;


	/**
	 * Construct a new stop watch.
	 * Does not start any task.
	 */
	public StopWatch() {
	}

	/**
	 * Construct a new stop watch with the given id.
	 * Does not start any task.
	 * @param id identifier for this stop watch.
	 * Handy when we have output from multiple stop watches and need to distinguish between them.
	 */
	public StopWatch(String id) {
		this.id = id;
	}
	
	/**
	 * Determine whether the TaskInfo array is built over time. Set this to
	 * false when using a stopwatch for millions of intervals, or the task
	 * info structure will consume excessive memory. Default is true.
	 * @param keepTaskList
	 */
	public void setKeepTaskList(boolean keepTaskList) {
		this.keepTaskList = keepTaskList;
	}

	/**
	 * Return whether to build the TaskInfo array over time.
	 */
	public boolean getKeepTaskList() {
		return this.keepTaskList;
	}


	/**
	 * Start a named task. The results are undefined if stop() or timing
	 * methods are called without invoking this method.
	 * @param taskName the name of the task to start
	 */
	public void start(String taskName) throws IllegalStateException {
		if (this.running) {
			throw new IllegalStateException("Can't start StopWatch: it's already running");
		}
		this.startTimeMillis = System.currentTimeMillis();
		this.currentTaskName = taskName;
		this.running = true;
	}

	/**
	 * Stop the current task. The results are undefined if timing methods are
	 * called without invoking at least one pair start()/stop() methods.
	 */
	public void stop() throws IllegalStateException {
		if (!this.running) {
			throw new IllegalStateException("Can't stop StopWatch: it's not running");
		}
		long lastTime = System.currentTimeMillis() - this.startTimeMillis;
		this.totalTimeMillis += lastTime;
		this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
		if (this.keepTaskList) {
			this.taskList.add(lastTaskInfo);
		}
		++this.taskCount;
		this.running = false;
		this.currentTaskName = null;
	}

	/**
	 * Return whether the stop watch is currently running.
	 */
	public boolean isRunning() {
		return this.running;
	}


	/**
	 * Return the time taken by the last task.
	 */
	public long getLastTaskTimeMillis() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tests run: can't get last interval");
		}
		return this.lastTaskInfo.getTimeMillis();
	}

	/**
	 * Return the time taken by the last task.
	 * @deprecated in favor of {@link #getLastTaskTimeMillis getLastTaskTimeMillis}
	 */
	public long getLastInterval() throws IllegalStateException {
		return getLastTaskTimeMillis();
	}

	/**
	 * Return the total time in milliseconds for all tasks.
	 */
	public long getTotalTimeMillis() {
		return totalTimeMillis;
	}

	/**
	 * Return the total time in milliseconds for all tasks.
	 * @deprecated in favor of {@link #getTotalTimeMillis getTotalTimeMillis}
	 */
	public long getTotalTime() {
		return totalTimeMillis;
	}

	/**
	 * Return the total time in seconds for all tasks.
	 */
	public double getTotalTimeSeconds() {
		return totalTimeMillis / 1000.0;
	}

	/**
	 * Return the total time in seconds for all tasks.
	 * @deprecated in favor of {@link #getTotalTimeSeconds getTotalTimeSeconds}
	 */
	public double getTotalTimeSecs() {
		return getTotalTimeSeconds();
	}

	/**
	 * Return the number of tasks timed.
	 */
	public int getTaskCount() {
		return taskCount;
	}

	/**
	 * Return an array of the data for tasks performed.
	 */
	public TaskInfo[] getTaskInfo() {
		if (!this.keepTaskList) {
			throw new UnsupportedOperationException("Task info is not being kept!");
		}
		return (TaskInfo[]) taskList.toArray(new TaskInfo[0]);
	}


	/**
	 * Return a short description of the total running time.
	 */
	public String shortSummary() {
		return ("StopWatch '" + id + "': running time (seconds) = " + getTotalTimeSeconds() + "\n");
	}

	/**
	 * Return a string with a table describing all tasks performed.
	 * For custom reporting, call getTaskInfo() and use the task info directly.
	 */
	public String prettyPrint() {
		StringBuffer sb = new StringBuffer(shortSummary());
		if (!this.keepTaskList) {
			sb.append("No task info kept");
		}
		else {
			TaskInfo[] tasks = getTaskInfo();
			sb.append("-----------------------------------------\n");
			sb.append("ms     %     Task name\n");
			sb.append("-----------------------------------------\n");
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMinimumIntegerDigits(5);
			nf.setGroupingUsed(false);
			NumberFormat pf = NumberFormat.getPercentInstance();
			pf.setMinimumIntegerDigits(3);
			pf.setGroupingUsed(false);
			for (int i = 0; i < tasks.length; i++) {
				sb.append(nf.format(tasks[i].getTimeMillis()) + "  ");
				sb.append(pf.format(tasks[i].getTimeSeconds() / getTotalTimeSeconds()) + "  ");
				sb.append(tasks[i].getTaskName() + "\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Return an informative string describing all tasks performed
	 * For custom reporting, call getTaskInfo() and use the task info directly.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(shortSummary());
		if (this.keepTaskList) {
			TaskInfo[] tasks = getTaskInfo();
			for (int i = 0; i < tasks.length; i++) {
				if (i > 0)
					sb.append("; ");
				sb.append("[" + tasks[i].getTaskName() + "] took " + tasks[i].getTimeSeconds());
				long percent = Math.round((100.0 * tasks[i].getTimeSeconds()) / getTotalTimeSeconds());
				sb.append("=" + percent + "%");
			}
		}
		else {
			sb.append("Not keeping task info");
		}
		return sb.toString();
	}


	/**
	 * Inner class to hold data about one task executed within the stop watch.
	 */
	public static class TaskInfo {

		private final String taskName;

		private final long timeMillis;

		private TaskInfo(String taskName, long timeMillis) {
			this.taskName = taskName;
			this.timeMillis = timeMillis;
		}

		/**
		 * Return the name of this task.
		 */
		public String getTaskName() {
			return taskName;
		}

		/**
		 * Return the time in milliseconds this task took.
		 */
		public long getTimeMillis() {
			return timeMillis;
		}

		/**
		 * Return the time in milliseconds this task took.
		 * @deprecated in favor of {@link #getTimeMillis getTimeMillis}
		 */
		public long getTime() {
			return timeMillis;
		}

		/**
		 * Return the time in seconds this task took.
		 */
		public double getTimeSeconds() {
			return timeMillis / 1000.0;
		}

		/**
		 * Return the time in seconds this task took.
		 * @deprecated in favor of {@link #getTimeSeconds getTimeSeconds}
		 */
		public double getTimeSecs() {
			return getTimeSeconds();
		}
	}

}
