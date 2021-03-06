/*
 * Copyright (C) 2011-2014 Volker Bergmann (volker.bergmann@bergmann-it.de).
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rapiddweller.contiperf.log;

import com.rapiddweller.contiperf.ExecutionLogger;
import com.rapiddweller.contiperf.report.InvocationLog;
import com.rapiddweller.contiperf.report.InvocationSummary;
import com.rapiddweller.contiperf.report.ListReportModule;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ExecutionLogger} implementation that stores all reported invocation logs and 
 * summaries in lists.<br/><br/>
 * Created: 29.03.2010 12:37:33
 * @since 1.0
 * @author Volker Bergmann
 * @deprecated replaced with {@link ListReportModule}
 */
@Deprecated
public class ListExecutionLogger implements ExecutionLogger {
	
	private final List<InvocationLog> invocations;
	private final List<InvocationSummary> summaries;
	
	public ListExecutionLogger() {
		this.invocations = new ArrayList<>();
		this.summaries = new ArrayList<>();
    }

	public void logInvocation(String id, int latency, long startTime) {
	    invocations.add(new InvocationLog(id, latency, startTime));
    }

	public void logSummary(String id, long elapsedTime, long invocationCount, long startTime) {
	    summaries.add(new InvocationSummary(id, elapsedTime, invocationCount, startTime));
    }

}
