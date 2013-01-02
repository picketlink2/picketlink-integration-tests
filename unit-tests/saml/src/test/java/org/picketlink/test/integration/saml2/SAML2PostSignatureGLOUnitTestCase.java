/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addKeyStoreAlias;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addTrustedDomain;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addValidatingAlias;
import static org.picketlink.test.integration.util.TestUtil.getServerAddress;
import static org.picketlink.test.integration.util.TestUtil.getTargetURL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.TargetContainers;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Unit test the GLO scenarios involving two endpoints with SAML2 Post and Signature binding
 * 
 * @author anil saldhana
 */
@TargetContainers ({"jbas5", "jbas6", "jbas7", "tomcat6", "eap5", "eap6"})
public class SAML2PostSignatureGLOUnitTestCase extends AbstractSAMLIntegrationTests {
    
    @Test
    public void testSAMLPostBindingGlobalLogOut() throws Exception {
        System.out.println("Trying " + getSalesURL());
        // Sales post Application Login
        WebRequest serviceRequest1 = new GetMethodWebRequest(getSalesURL());
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
        System.out.println("Trying " + getEmployeeURL());
        webResponse = webConversation.getResponse(getEmployeeURL());
        assertTrue(" Reached the employee index page ", webResponse.getText().contains("EmployeeDashboard"));

        // Logout from sales
        System.out.println("Trying " + getSalesURL() + LOGOUT_URL);
        webResponse = webConversation.getResponse(getSalesURL() + LOGOUT_URL);
        assertTrue("Reached logged out page", webResponse.getText().contains("Logout"));

        // Hit the Sales Apps again
        System.out.println("Trying " + getSalesURL());
        webResponse = webConversation.getResponse(getSalesURL());
        assertTrue(" Reached the Login page ", webResponse.getText().contains("Login"));

        // Hit the Employee Apps again
        System.out.println("Trying " + getEmployeeURL());
        webResponse = webConversation.getResponse(getEmployeeURL());
        assertTrue(" Reached the Login page ", webResponse.getText().contains("Login"));

        webConversation.clearContents();
    }

    protected String getEmployeeURL() {
        return getTargetURL("/employee-sig/");
    }
    
    protected String getSalesURL() {
        return getTargetURL("/sales-post-sig/");
    }
    
    @Deployment(name = "idp-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createIDPSigDeployment() throws GeneralSecurityException, IOException {
        WebArchive idp = MavenArtifactUtil.getQuickstartsMavenArchive("idp-sig");
        
        addTrustedDomain(idp, getServerAddress());
        addValidatingAlias(idp, getServerAddress(), getServerAddress());
        addKeyStoreAlias(idp, getServerAddress());
        
        return idp;
    }
    
    @Deployment(name = "sales-post-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesPostSigDeployment() throws GeneralSecurityException, IOException {
        WebArchive sp = MavenArtifactUtil.getQuickstartsMavenArchive("sales-post-sig");
        
        addValidatingAlias(sp, getServerAddress(), getServerAddress());
        addKeyStoreAlias(sp, getServerAddress());
        
        return sp;
    }
    
    @Deployment(name = "employee-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createEmployeeSigDeployment() throws KeyStoreException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, GeneralSecurityException, IOException {
        WebArchive sp = MavenArtifactUtil.getQuickstartsMavenArchive("employee-sig");
        
        addValidatingAlias(sp, getServerAddress(), getServerAddress());
        addKeyStoreAlias(sp, getServerAddress());
        
        return sp;
    }
}