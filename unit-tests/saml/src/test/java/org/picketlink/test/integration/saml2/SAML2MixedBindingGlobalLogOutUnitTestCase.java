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

import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addTrustedDomain;
import static org.picketlink.test.integration.util.TestUtil.getServerAddress;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.TargetContainers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * <p>
 * Unit test the SAML2 Global Log Out scenarios.
 * </p>
 * <p>
 * <b>Note:</b> This test expects that a set of endpoints that are configured for the test are available. You may have to start
 * web containers offline for the endpoints to be live.
 * </p>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Apr 8, 2010
 */
@TargetContainers ({"jbas5", "jbas6", "jbas7", "tomcat6", "eap5", "eap6"})
public class SAML2MixedBindingGlobalLogOutUnitTestCase extends AbstractSAMLIntegrationTests {

    @Test
    public void testSAMLMixedBindingWithPostFirstGlobalLogOut() throws Exception {
        hitURLs();
    }

    private void hitURLs() throws Exception {
        System.out.println("Trying " + SALES_POST_URL);

        // Sales post Application Login
        WebRequest serviceRequest1 = new GetMethodWebRequest(SALES_POST_URL);
        WebConversation webConversation = new WebConversation();

        WebResponse webResponse = webConversation.getResponse(serviceRequest1);
        
        WebForm loginForm = webResponse.getForms()[0];
        loginForm.setParameter("j_username", "tomcat");
        loginForm.setParameter("j_password", "tomcat");
        SubmitButton submitButton = loginForm.getSubmitButtons()[0];
        submitButton.click();

        webResponse = webConversation.getCurrentPage();
        
        assertTrue(" Reached the sales index page ", webResponse.getText().contains("SalesTool"));

        // Employee post Application Login
        System.out.println("Trying " + EMPLOYEE_REDIRECT_URL);
        webResponse = webConversation.getResponse(EMPLOYEE_REDIRECT_URL);
        assertTrue(" Reached the employee index page ", webResponse.getText().contains("EmployeeDashboard"));

        // Employee Redirect Valve Application Login
        System.out.println("Trying " + EMPLOYEE_REDIRECT_VALVE_URL);
        webResponse = webConversation.getResponse(EMPLOYEE_REDIRECT_VALVE_URL);
        assertTrue(" Reached the employee index page ", webResponse.getText().contains("EmployeeDashboard"));

        // Sales Post Valve Application Login
        System.out.println("Trying " + SALES_POST_VALVE_URL);
        webResponse = webConversation.getResponse(SALES_POST_VALVE_URL);
        assertTrue(" Reached the employee index page ", webResponse.getText().contains("SalesTool"));

        // Logout from sales
        System.out.println("Trying " + EMPLOYEE_REDIRECT_URL + LOGOUT_URL);
        webResponse = webConversation.getResponse(EMPLOYEE_REDIRECT_URL + LOGOUT_URL);
        assertTrue("Reached logged out page", webResponse.getText().contains("Logout"));

        // Hit the Sales Apps again
        System.out.println("Trying " + SALES_POST_URL);
        webResponse = webConversation.getResponse(SALES_POST_URL);
        assertTrue(" Reached the Login page ", webResponse.getText().contains("Login"));

        // Hit the Employee Apps again
        System.out.println("Trying " + EMPLOYEE_REDIRECT_URL);
        webResponse = webConversation.getResponse(EMPLOYEE_REDIRECT_URL);
        assertTrue(" Reached the Login page ", webResponse.getText().contains("Login"));

        // Hit the Sales Valve Apps again
        System.out.println("Trying " + SALES_POST_VALVE_URL);
        webResponse = webConversation.getResponse(SALES_POST_VALVE_URL);
        assertTrue(" Reached the Login page ", webResponse.getText().contains("Login"));

        webConversation.clearContents();
    }

    @Deployment(name = "idp", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createIDPDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        WebArchive idp = MavenArtifactUtil.getQuickstartsMavenArchive("idp");
        
        addTrustedDomain(idp, getServerAddress());
        
        return idp;
    }
    
    @Deployment(name = "sales-post", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesPostDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("sales-post");
    }

    @Deployment(name = "sales-post-valve", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesPostValveDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("sales-post-valve");
    }

    @Deployment(name = "employee", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createEmployeeDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("employee");
    }

    @Deployment(name = "employee-redirect-valve", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createEmployeeRedirectValvePostDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("employee-redirect-valve");
    }
}
