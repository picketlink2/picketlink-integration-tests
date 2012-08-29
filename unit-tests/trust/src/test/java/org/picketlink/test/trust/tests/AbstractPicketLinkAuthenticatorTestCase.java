/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.trust.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.bindings.tomcat.PicketLinkAuthenticator;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TestUtil;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * <p>Abstract class for testing the {@link PicketLinkAuthenticator}.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@RunWith(PicketLinkIntegrationTests.class)
public abstract class AbstractPicketLinkAuthenticatorTestCase {

    @Test
    public void testDistinctUsers() throws Exception {
        WebRequest serviceRequest1 = new GetMethodWebRequest(TestUtil.getTargetURL("/authenticator/?user=UserA"));
        WebConversation webConversation = new WebConversation();
        WebResponse webResponse = webConversation.getResponse(serviceRequest1);
        String responseText = webResponse.getText();
        
        assertTrue(responseText.contains("UserA"));

        WebRequest serviceRequest2 = new GetMethodWebRequest(TestUtil.getTargetURL("/authenticator/?user=UserB"));
        WebConversation webConversation2 = new WebConversation();
        WebResponse webResponse2 = webConversation2.getResponse(serviceRequest2);
        String responseText2 = webResponse2.getText();
        assertTrue(responseText2.contains("UserB"));
    }
    
    protected static File getTestFile(String path) {
        return new File("../../unit-tests/trust/target/test-classes/picketlink-authenticator/" + path);
    }

}
