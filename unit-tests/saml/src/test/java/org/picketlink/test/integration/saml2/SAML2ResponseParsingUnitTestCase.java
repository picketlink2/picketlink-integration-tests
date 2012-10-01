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
package org.picketlink.test.integration.saml2;

import static org.junit.Assert.assertEquals;
import static org.picketlink.test.integration.util.TestUtil.getTargetURL;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.TargetContainers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Test for parsing the SAML2 Response
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jul 26, 2011
 */
@TargetContainers ({"jbas5", "jbas6", "jbas7", "tomcat6", "eap5"})
public class SAML2ResponseParsingUnitTestCase extends AbstractSAMLIntegrationTests {
    
    /**
     * PLFED-214: Uses the claims.war in the picketlink-int-webapps setup
     */
    @Test
    public void testADFSClaims() throws Exception {
        WebRequest serviceRequest1 = new GetMethodWebRequest(getTargetURL("/claims/claimsprocess.jsp"));
        WebConversation webConversation = new WebConversation();

        WebResponse webResponse = webConversation.getResponse(serviceRequest1);
        assertEquals(200, webResponse.getResponseCode());

        webConversation.clearContents();
    }
    
    @Deployment(name = "claims", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createClaimsDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        
        archive.addAsManifestResource(getTestFile("META-INF/jboss-deployment-structure.xml"));
        
        archive.addAsWebInfResource(getTestFile("WEB-INF/web.xml"));
        archive.addAsWebInfResource(getTestFile("WEB-INF/jboss-web.xml"));
        
        archive.addAsWebResource(getTestFile("claimsprocess.jsp"));
        archive.addAsWebResource(getTestFile("saml2-response-adfs-claims.xml"));

        return archive;
    }
    
    protected static File getTestFile(String path) {
        return new File("../../unit-tests/saml/target/test-classes/claims/" + path);
    }

}