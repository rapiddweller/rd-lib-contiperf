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
package com.rapiddweller.profile;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link Profiler}.<br/><br/>
 * Created: 19.05.2011 09:43:47
 *
 * @author Volker Bergmann
 * @since 2.0.0
 */
public class ProfilerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {

        // given
        Profiler profiler = new Profiler("test", 100);

        //when
        List<String> path = new ArrayList<String>();
        profiler.addSample(path, 1000);
        path.add("sub1");
        profiler.addSample(path, 200);
        path.remove(path.size() - 1);
        path.add("sub2");
        profiler.addSample(path, 300);
        profiler.addSample(path, 400);

        // then
        Profile rootProfile = profiler.getRootProfile();
        assertEquals(1, rootProfile.getInvocationCount());
        assertEquals(10., rootProfile.getAverageLatency(), 0.01);
        assertEquals(10, rootProfile.getTotalLatency());

        Profile sub1Profile = rootProfile.getOrCreateSubProfile("sub1");
        assertEquals(1, sub1Profile.getInvocationCount());
        assertEquals(2., sub1Profile.getAverageLatency(), 0.01);
        assertEquals(2, sub1Profile.getTotalLatency());

        Profile sub2Profile = rootProfile.getOrCreateSubProfile("sub2");
        assertEquals(2, sub2Profile.getInvocationCount());
        assertEquals(3.5, sub2Profile.getAverageLatency(), 0.01);
        assertEquals(7, sub2Profile.getTotalLatency());

        profiler.printSummary();
    }

    @Test
    public void testConstructor() {
        Profile rootProfile = (new Profiler("Name", 1L)).getRootProfile();
        assertEquals(0L, rootProfile.getTotalLatency());
        assertNull(rootProfile.getParent());
        assertEquals(0L, rootProfile.getInvocationCount());
        assertEquals("[0 inv., avg: NaN, total: 0]: Name", rootProfile.toString());
        assertEquals("Name", rootProfile.getName());
    }

    @Test
    public void testDefaultInstance() {
        // TODO: This test is incomplete.
        //   Reason: No meaningful assertions found.
        //   To help Diffblue Cover to find assertions, please add getters to the
        //   class under test that return fields written by the method under test.
        //   See https://diff.blue/R004

        Profiler.defaultInstance();
    }

    @Test
    public void testAddSample() {
        Profiler profiler = new Profiler("Name", 1L);
        profiler.addSample(new ArrayList<String>(), 1L);
        Profile rootProfile = profiler.getRootProfile();
        assertEquals(1L, rootProfile.getTotalLatency());
        assertEquals(1L, rootProfile.getInvocationCount());
    }

    @Test
    public void testAddSample2() {
        Profiler profiler = new Profiler("Name", 0L);
        thrown.expect(ArithmeticException.class);
        profiler.addSample(new ArrayList<String>(), 1L);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void testAddSample3() {
        Profiler profiler = new Profiler("Name", -1L);
        profiler.addSample(new ArrayList<>(), 1L);
    }

    @Test
    public void testAddSample4() {
        Profiler profiler = new Profiler("Name", 1L);
        ArrayList<String> stringList = new ArrayList<String>();
        stringList.add("E");
        profiler.addSample(stringList, 1L);
        assertEquals(profiler.getRootProfile().getName(), "Name");
    }

    @Test
    public void testPrintSummary() {
        Profiler profiler = new Profiler("Name", 1L);
        profiler.printSummary();
    }

}
