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

import org.junit.runners.model.RunnerScheduler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * {@link RunnerScheduler} which executes all tests in parallel.<br/><br/>
 * Created: 08.04.2012 07:11:38
 * @since 2.1.0
 * @author Volker Bergmann
 */
public class ParallelScheduler implements RunnerScheduler {
	
	private final Queue<Future<String>> tasks = new LinkedList<>();
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final CompletionService<String> completionService =
			new ExecutorCompletionService<>(executorService);

	public void schedule(final Runnable childStatement) {
		Future<String> future = completionService.submit(new Callable<>() {
			public String call() {
				childStatement.run();
				return toString();
			}

			@Override
			public String toString() {
				return childStatement.toString();
			}
		});
		tasks.add(future);
	}

	public void finished() {
		try {
			while (!tasks.isEmpty()) {
				Future<String> task = completionService.take();
				//System.out.println("Completed " + task.get());
				tasks.remove(task);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			while (!tasks.isEmpty())
				tasks.poll().cancel(true);
			executorService.shutdownNow();
		}
	}
	
}
