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
package com.rapiddweller.contiperf.report;

/**
 * Holds the data of an invocation log entry.<br/><br/>
 * Created: 29.03.2010 12:38:26
 * @since 1.0
 * @author Volker Bergmann
 */
public class InvocationLog {

	public final String id;
	public final int latency;
	public final long startTime;
	
	public InvocationLog(String id, int latency, long startTime) {
	    this.id = id;
	    this.latency = latency;
	    this.startTime = startTime;
    }
	
}
