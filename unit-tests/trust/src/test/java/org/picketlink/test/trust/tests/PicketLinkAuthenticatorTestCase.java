/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.bindings.tomcat.PicketLinkAuthenticator;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.TestUtil;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Test the {@link PicketLinkAuthenticator}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Sep 13, 2011
 */
@RunWith(Arquillian.class)
public class PicketLinkAuthenticatorTestCase {
    
    @Deployment(name = "authenticator", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createAuthenticatorDeployment() {
        return MavenArtifactUtil.getIntegrationMavenArchive("authenticator");
    }
    
    @Deployment(name = "picketlink-wstest-tests", testable = false)
    @TargetsContainer("jboss")
    public static JavaArchive createWSTestDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("../../unit-tests/trust/target/picketlink-wstest-tests.jar"));
    }

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
}