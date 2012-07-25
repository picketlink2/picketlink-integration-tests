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
package org.picketlink.test.integration.saml11;

import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addTrustedDomain;
import static org.picketlink.test.integration.util.TestUtil.getServerAddress;
import static org.picketlink.test.integration.util.TestUtil.getTargetURL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.test.integration.saml2.AbstractSAMLIntegrationTests;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.TargetContainers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * <p>
 * Unit test the PicketLink IDP application that supports the SAML v1.1 interaction.
 * </p>
 * <p>
 * <b>Note:</b> This test expects that a set of endpoints that are configured for the test are available. You may have to start
 * web containers offline for the endpoints to be live.
 * </p>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jul 7, 2011
 */
@TargetContainers ({"jbas5", "jbas6", "jbas7", "tomcat6", "eap5"})
public class SAML11IDPFirstUnitTestCase extends AbstractSAMLIntegrationTests {

    @Test
    public void testSAML11() throws Exception {
        String idpURL = getTargetURL("/idp/");

        System.out.println("Trying " + idpURL);
        WebRequest serviceRequest1 = new GetMethodWebRequest(idpURL);
        WebConversation webConversation = new WebConversation();

        WebResponse webResponse = webConversation.getResponse(serviceRequest1);
        WebForm loginForm = webResponse.getForms()[0];
        loginForm.setParameter("j_username", "tomcat");
        loginForm.setParameter("j_password", "tomcat");
        SubmitButton submitButton = loginForm.getSubmitButtons()[0];
        submitButton.click();

        webResponse = webConversation.getCurrentPage();
        assertTrue(" Reached the sales index page ", webResponse.getText().contains("Sales"));

        WebLink[] links = webResponse.getLinks();
        boolean foundLink = false;
        for (WebLink webLink : links) {
            if (webLink.getURLString().contains("sales-saml11")) {
                foundLink = true;
                webResponse = webLink.click();
                assertTrue(" Reached the sales index page ", webResponse.getText().contains("SalesTool"));
                break;
            }
        }
        assertTrue("We found the SP link?", foundLink);
    }
    
    @Deployment(name = "idp", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createIDPDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        WebArchive idp = MavenArtifactUtil.getQuickstartsMavenArchive("idp");
        
        addTrustedDomain(idp, getServerAddress());
        
        return idp;
    }
    
    @Deployment(name = "sales-saml11", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesSAML11Deployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("sales-saml11");
    }
}