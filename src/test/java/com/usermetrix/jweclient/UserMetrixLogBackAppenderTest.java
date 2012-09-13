/*
 * UserMetrixLogBackAppenderTest.java
 * UserMetrix-LogBackApppender
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import com.usermetrix.jweclient.TestConstants;

public class UserMetrixLogBackAppenderTest {
    private static Logger logger = LoggerFactory.getLogger(UserMetrixLogBackAppenderTest.class);

    @Test
    public void testAppend() throws Exception {
        // Must push a universally unique identifier into the MDC before using any slf4j calls.
        // The UUID must not contain any hyphens - i.e. "19C1AA26B8064108956DBB13D4D626F1"
        MDC.put("sessionId", TestConstants.UUID1);
        logger.error("my error");

        // Test that a UserMetrix log file is created and correctly formed.        
        File log = TestConstants.logFileFor(TestConstants.UUID1);
        Yaml yaml = new Yaml();
        InputStream in = new FileInputStream(log);

        Map<String, Object> logContents = (Map<String, Object>) yaml.load(in);
        ArrayList<Map<String, Object> > logStream = (ArrayList<Map<String, Object> >) logContents.get("log");

        Map<String, Object> logItem = logStream.get(0);
        assertEquals("error", logItem.get("type"));
        assertEquals("class com.usermetrix.jweclient.UserMetrixLogBackAppenderTest", logItem.get("source"));
        assertEquals("my error", logItem.get("message"));
        assertEquals(logStream.size(), 1);
        
        UserMetrix.shutdown(TestConstants.UUID1);
        assertFalse(log.exists());
    }
}
