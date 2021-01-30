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
package com.rapiddweller.contiperf.junit;

import java.util.concurrent.atomic.AtomicLong;

import com.rapiddweller.contiperf.ArgumentsProvider;
import com.rapiddweller.contiperf.Clock;
import com.rapiddweller.contiperf.EmptyArgumentsProvider;
import com.rapiddweller.contiperf.ExecutionConfig;
import com.rapiddweller.contiperf.InvocationRunner;
import com.rapiddweller.contiperf.Invoker;
import com.rapiddweller.contiperf.ConcurrentRunner;
import com.rapiddweller.contiperf.PerfTestConfigurationError;
import com.rapiddweller.contiperf.PerfTestExecutionError;
import com.rapiddweller.contiperf.PerformanceTracker;
import com.rapiddweller.contiperf.PerformanceRequirement;
import com.rapiddweller.contiperf.CountRunner;
import com.rapiddweller.contiperf.TimedRunner;
import com.rapiddweller.contiperf.WaitTimer;
import com.rapiddweller.contiperf.report.ReportContext;
import org.junit.runners.model.Statement;

/**
 * Implementation of {@link org.junit.runners.model.Statement} which wraps another Statement 
 * and adds multiple invocation, execution timing and duration check.<br/><br/>
 * Created: 12.10.2009 07:37:47
 * @since 1.0
 * @author Volker Bergmann
 */
final class PerfTestStatement extends Statement {
	
    private final String id;
    private final Statement base;
    private final ReportContext context;
    private final ExecutionConfig config;
    private final PerformanceRequirement requirement;

    PerfTestStatement(Statement base, String id, ExecutionConfig config, 
    		PerformanceRequirement requirement, ReportContext context) {
	    this.base = base;
	    this.id = id;
	    this.config = config;
	    this.requirement = requirement;
	    this.context = context;
    }

    @Override
    public void evaluate() {
		System.out.println(id);
    	Invoker invoker = new JUnitInvoker(id, base);
    	Clock[] clocks = config.getClocks();
    	PerformanceTracker tracker = new PerformanceTracker(invoker, config, requirement, context, clocks);
		try (InvocationRunner runner = createRunner(tracker)) {
			runner.run();
			if (!tracker.isTrackingStarted() && config.getWarmUp() > 0)
				throw new PerfTestExecutionError(
						"Test finished before warm-up period (" +
								config.getWarmUp() + " ms) was over");
		} finally {
			if (tracker.isTrackingStarted())
				tracker.stopTracking();
			tracker.clear();
		}
    }

    private InvocationRunner createRunner(PerformanceTracker tracker) {
	    ArgumentsProvider provider = new EmptyArgumentsProvider();
	    InvocationRunner runner;
        int threads = config.getThreads();
		int rampUp = config.getRampUp();
		int durationWithRampUp = config.getDuration() + config.getRampUp() * (config.getThreads() - 1);
		int invocations = config.getInvocations();
		WaitTimer waitTimer = config.getWaitTimer();
		if (config.getDuration() > 0) {
			if (threads == 1) {
				// single-threaded timed test
				runner = new TimedRunner(tracker, provider, waitTimer, durationWithRampUp);
			} else {
				// multi-threaded timed test
				if (durationWithRampUp - (threads - 1) * rampUp <= 0)
					throw new IllegalArgumentException("test duration is shorter than the cumulated ramp-up times");
				InvocationRunner[] runners = new InvocationRunner[threads];
				for (int i = 0; i < threads; i++)
					runners[i] = new TimedRunner(tracker, provider, waitTimer, durationWithRampUp - i * rampUp);
				runner = new ConcurrentRunner(id, runners, rampUp);
			}
    	} else if (invocations >= 0) {
    		AtomicLong counter = new AtomicLong(invocations);
    		if (threads == 1) {
    			// single-threaded count-based test
    			runner = new CountRunner(tracker, provider, waitTimer, counter);
    		} else {
    			// multi-threaded count-based test
    			InvocationRunner[] runners = new InvocationRunner[threads];
	        	for (int i = 0; i < threads; i++)
	        		runners[i] = new CountRunner(tracker, provider, waitTimer, counter);
				runner = new ConcurrentRunner(id, runners, rampUp);
    		}
        } else 
        	throw new PerfTestConfigurationError("No useful invocation count or duration defined");
	    return runner;
    }
    
}