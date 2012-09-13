/*
 * UserMetrixLogBackAppender.java
 * UserMetrix-LogBackAppender
 *
 * Copyright (c) 2012 UserMetrix Pty Ltd. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.usermetrix.jweclient;

import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;


public class UserMetrixLogBackAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        Class<?> sourceClass;
        try {
            sourceClass = Class.forName(loggingEvent.getLoggerName());
        } catch (ClassNotFoundException e) {
            // Unable to find the class that we are logging - so just use the
            // base object as the source name.
            sourceClass = Object.class;
        }

        switch (loggingEvent.getLevel().toInt()) {
            case Level.ERROR_INT:
            case Level.WARN_INT:

            	try {
	                // If logging event contains details of something that has been
	                // thrown - pump that into the UserMetrix logging event.
	                if (loggingEvent.getThrowableProxy() != null &&
	                    loggingEvent.getThrowableProxy().getClass().equals(ThrowableProxy.class)) {
	                    ThrowableProxy tp = (ThrowableProxy) loggingEvent.getThrowableProxy();
	                    
	                    String session = MDC.get("sessionId");
	                    if (session != null) {	                    
	                    	UserMetrix.error(session, sourceClass, loggingEvent.getMessage(), tp.getThrowable());
	                    }

	                // Nothing has been thrown - just do a vanilla error log event.
	                } else {
	                	String session = MDC.get("sessionId");
	                	if (session != null) {
	                		UserMetrix.error(session ,sourceClass, loggingEvent.getMessage());
	                	}
	                }
            	} catch (Exception e) {
            		System.err.println("Warning: Unable capture information with UserMetrix: " + e);
            	}

                break;

            default:
            	// We ignore all the other logging levels - we are really only interested in
            	// errors and warnings.
                break;
        }
    }

    @Override
    public void stop() {
    	// Shutdown all the remaining usermetrix logs and transmit them to the server.
    	UserMetrix.shutdownAll();

        super.stop();
    }
}

