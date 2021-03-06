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
package com.rapiddweller.contiperf.timer;

import com.rapiddweller.contiperf.WaitTimer;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CumulatedTimer}.<br/><br/>
 * Created: 06.04.2012 18:19:40
 * @since 2.1.0
 * @author Volker Bergmann
 */
public class CumulatedTimerTest {

	@Test
	public void testEmptyInitialization() throws Exception {
		WaitTimer timer = CumulatedTimer.class.getDeclaredConstructor().newInstance();
		timer.init(new double[0]);
		for (int i = 0; i < 1000; i++)
			assertRange(500, 1500, timer.getWaitTime());
	}

	@Test
	public void testUnderInitialization() throws Exception {
		WaitTimer timer = CumulatedTimer.class.getDeclaredConstructor().newInstance();
		timer.init(new double[] { 2000 });
		for (int i = 0; i < 1000; i++)
			assertRange(2000, 3000, timer.getWaitTime());
	}

	@Test
	public void testNormalInitialization() throws Exception {
		WaitTimer timer = CumulatedTimer.class.getDeclaredConstructor().newInstance();
		timer.init(new double[] { 2000, 2500 });
		for (int i = 0; i < 1000; i++)
			assertRange(2000, 2500, timer.getWaitTime());
	}

	@Test
	public void testOverInitialization() throws Exception {
		WaitTimer timer = CumulatedTimer.class.getDeclaredConstructor().newInstance();
		timer.init(new double[] { 2000, 2500, 3000 });
		for (int i = 0; i < 1000; i++)
			assertRange(2000, 2500, timer.getWaitTime());
	}

	private static void assertRange(int minExpected, int maxExpected, int waitTime) {
		assertTrue(minExpected <= waitTime && waitTime <= maxExpected);
	}
	
}
