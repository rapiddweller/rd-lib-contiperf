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

import com.rapiddweller.contiperf.Invoker;
import com.rapiddweller.contiperf.util.ContiPerfUtil;
import org.junit.runners.model.Statement;

/**
 * {@link Invoker} implementation for JUnit 4.7+.<br/><br/>
 * Created: 22.10.2009 16:55:12
 * @since 1.0
 * @author Volker Bergmann
 */
public class JUnitInvoker implements Invoker {
	
	private final String id;
	private final Statement base;

	public JUnitInvoker(String id, Statement base) {
	    this.id = id;
	    this.base = base;
    }

	public String getId() {
		return id;
	}

	public Object invoke(Object[] args) {
		try {
	        base.evaluate();
	        return null;
        } catch (Throwable e) {
        	throw ContiPerfUtil.executionError(e);
        }
	}

}
