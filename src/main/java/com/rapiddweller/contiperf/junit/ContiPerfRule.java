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

import com.rapiddweller.contiperf.*;
import com.rapiddweller.contiperf.log.ConsoleExecutionLogger;
import com.rapiddweller.contiperf.report.HtmlReportModule;
import com.rapiddweller.contiperf.report.LoggerModuleAdapter;
import com.rapiddweller.contiperf.report.ReportContext;
import com.rapiddweller.contiperf.report.ReportModule;
import com.rapiddweller.contiperf.util.ContiPerfUtil;
import junit.framework.AssertionFailedError;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Implements the JUnit {@link MethodRule} interface 
 * for adding performance test features to test calls.
 * 
 * <p>for activating it, add an attribute of this class 
 * to your test class, e.g.:
 * <pre>
 * public class SimpleTest {
 *     @Rule
 *     public ContiPerfRule i = new ContiPerfRule();
 * 
 *     @Test
 *     public void sleepAWhile() throws Exception {
 *         Thread.sleep(100);
 *     }
 * }
 * </pre> 
 * ContiPerf will then intercept each test method call
 * and optionally cause multiple invocations and check 
 * total execution time against a time limit.</p>
 * 
 * <p>invocation counts and time limits can be configured 
 * by Java annotations, e.g.
 * <pre>
 * @Test(timeout = 300)
 * @PerfTest(invocations = 5, timeLimit = 500)
 * public void sleepALittleLonger() throws Exception {
 *     System.out.print('x');
 *     Thread.sleep(200);
 * }
 * </pre>
 * </p>
 * 
 * <p>For enabling different test settings, the invocation count
 * values can be configured in a properties file <code>contiperf.properties</code>
 * which assigns the invocation count to the fully qualified method name, e.g.
 * <pre>
 * com.rapiddweller.contiperf.junit.SimpleTest.sleepAWhile=3
 * com.rapiddweller.contiperf.junit.SimpleTest.sleepALittleLonger=2
 * </pre>
 * If the properties file exists, it overrides the annotation values.</p>
 * 
 * <p>By default, the execution times are written to the CSV file  
 * <code>target/contiperf/contiperf.log</code>. They have four columns,
 * listing the
 * <ol>
 *     <li>fully qualified method name</li>
 *     <li>total execution time</li>
 *     <li>invocation count</li>
 *     <li>start time in milliseconds since 1970-01-01</li>
 * </ol></p>
 * 
 * <p>For reusing integration tests as performance tests, you can suppress
 * ContiPerf execution by setting the System property <code>contiperf.active=false</code>. 
 * </p>
 * <br/><br/>
 * Created: 12.10.2009 07:36:02
 * @since 1.0
 * @author Volker Bergmann
 */
@SuppressWarnings("deprecation")
public class ContiPerfRule implements MethodRule {
	
	private ExecutionConfig defaultExecutionConfig;
	ReportContext context;
	private PerformanceRequirement defaultRequirements;
	
	// constructors ----------------------------------------------------------------------------------------------------
	
   public ContiPerfRule() {
	    this((ReportContext) null);
    }

	public ContiPerfRule(ExecutionLogger logger) {
		this(createReportContext(logger), null);
    }

	public ContiPerfRule(ReportModule... modules) {
		this(createReportContext(modules), null);
    }

	protected ContiPerfRule(ReportContext context) {
		this(context, null);
    }

	protected ContiPerfRule(ReportContext context, Object suite) {
		if (context == null)
			this.context = JUnitReportContext.createInstance(suite);
		else
			this.context = context;
		if (suite != null) {
			Class<?> suiteClass = suite.getClass();
			this.defaultExecutionConfig = configurePerfTest(suiteClass.getAnnotation(PerfTest.class), suiteClass.getName());
			this.defaultRequirements = ContiPerfUtil.mapRequired(suiteClass.getAnnotation(Required.class));
		}
    }
	
	// static factory methods ------------------------------------------------------------------------------------------

	public static ContiPerfRule createDefaultRule() {
		ReportContext context = new JUnitReportContext();
		context.addReportModule(new HtmlReportModule());
		return new ContiPerfRule(context);
	}
	
	public static ContiPerfRule createVerboseRule() {
		ReportContext context = new JUnitReportContext();
		context.addReportModule(new HtmlReportModule());
		context.addReportModule(new LoggerModuleAdapter(new ConsoleExecutionLogger()));
		return new ContiPerfRule(context);
	}
	
	// MethodRule interface implementation -----------------------------------------------------------------------------

	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		// check if ContiPerf is active, if not then ignore the annotation
		Config config = Config.instance();
		if (!config.active())
			return base;
		// remove RunBefores and RunAfters from the statement cascade
		RunBefores runBefores = null;
		RunAfters runAfters = null;
		while (base instanceof RunAfters || base instanceof RunBefores) {
			try {
				if (base instanceof RunAfters) {
					runAfters = (RunAfters) base;
					Field next = base.getClass().getDeclaredField("next");
					next.setAccessible(true);
					base = (Statement) next.get(base);
				} else if (base instanceof RunBefores) {
					runBefores = (RunBefores) base;
					Field next = base.getClass().getDeclaredField("next");
					next.setAccessible(true);
					base = (Statement) next.get(base);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// set up report context
		if (context.getReportModules().size() == 0) {
			context = JUnitReportContext.createInstance(target);
			if (context.getReportModules().size() == 0)
				context = Config.instance().createDefaultReportContext(AssertionFailedError.class);
		}
		// create perf test statement
	    String testId = methodName(method, target);
		base = new PerfTestStatement(base, testId, executionConfig(method, testId), 
				requirements(method, testId), context);
		try {
			// if runBefores has been removed, reapply it
			if (runBefores != null) {
				Field next = runBefores.getClass().getDeclaredField("next");
				next.setAccessible(true);
				next.set(runBefores, base);
				base = runBefores;
			}
			// if runAfters has been removed, reapply it
			if (runAfters != null) {
				Field next = runAfters.getClass().getDeclaredField("next");
				next.setAccessible(true);
				next.set(runAfters, base);
				base = runAfters;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return base;
    }
	
	public ReportContext getContext() {
    	return context;
    }

	void setContext(ReportContext context) {
		this.context = context;
	}
	
	
	// helpers ---------------------------------------------------------------------------------------------------------
	
	private static ReportContext createReportContext(ExecutionLogger logger) {
		ReportContext context = new JUnitReportContext();
		context.addReportModule(new LoggerModuleAdapter(logger));
		return context;
	}

	private static ReportContext createReportContext(ReportModule... modules) {
		ReportContext context = new JUnitReportContext();
		for (ReportModule module : modules)
			context.addReportModule(module);
		return context;
	}

	private ExecutionConfig executionConfig(FrameworkMethod method, String methodName) {
		PerfTest annotation = annotationOfMethodOrClass(method, PerfTest.class);
        if (annotation != null)
        	return configurePerfTest(annotation, methodName);
        if (defaultExecutionConfig != null)
        	return defaultExecutionConfig;
        return new ExecutionConfig(1);
	}

	private PerformanceRequirement requirements(FrameworkMethod method, String testId) {
		Required annotation = annotationOfMethodOrClass(method, Required.class);
		if (annotation != null)
			return ContiPerfUtil.mapRequired(annotation);
        if (defaultRequirements != null)
        	return defaultRequirements;
		return null;
    }

	private static <T extends Annotation> T annotationOfMethodOrClass(FrameworkMethod method, Class<T> annotationClass) {
		T methodAnnotation = method.getAnnotation(annotationClass);
		if (methodAnnotation != null)
			return methodAnnotation;
		return method.getMethod().getDeclaringClass().getAnnotation(annotationClass);
	}
	
	public static ExecutionConfig configurePerfTest(PerfTest annotation, String testId) {
		ExecutionConfig config = ContiPerfUtil.mapPerfTestAnnotation(annotation);
		if (annotation == null)
			config = new ExecutionConfig(1);
		int count = Config.instance().getInvocationCount(testId);
		if (count >= 0)
			config.setInvocations(count);
		return config;
    }

	private static String methodName(FrameworkMethod method, Object target) {
		return target.getClass().getName() + '.' + method.getName(); 
		// no need to check signature: JUnit test methods do not have parameters
	}

}
