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

import java.io.File;
import java.security.Security;
import java.util.Hashtable;

import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.bindings.jboss.auth.SAML2STSLoginModule;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;
import org.picketlink.test.trust.ejb.EchoService;
import org.picketlink.test.trust.ejb.EchoServiceImpl;
import org.w3c.dom.Element;

import com.sun.security.sasl.Provider;

/**
 * <p>Tests the invocation of EJBs protected by the {@link SAML2STSLoginModule}.</p>
 * 
 * @author Anil Saldhana
 * @since Oct 3, 2010
 */
@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers({ "disabled" })
public class EJBAuthorizationAS7TestCase extends TrustTestsBase  {

    @Deployment(name = "ejb-test", testable = false)
    @TargetsContainer("jboss")
    public static JavaArchive createEJBTestDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "ejb-test.jar");

        archive.addClass(EchoService.class);
        archive.addClass(EchoServiceImpl.class);
        archive.addAsManifestResource(new File(EJBAuthorizationAS7TestCase.class.getClassLoader().getResource("jboss-deployment-structure.xml").getPath()));
        archive.addAsResource(new File(EJBAuthorizationAS7TestCase.class.getClassLoader().getResource("props/sts-users.properties").getPath()), ArchivePaths.create("users.properties"));
        archive.addAsResource(new File(EJBAuthorizationAS7TestCase.class.getClassLoader().getResource("props/sts-roles.properties").getPath()), ArchivePaths.create("roles.properties"));
        
        return archive;
    }

    @Test
    public void testSuccessfulEJBInvocation() throws Exception {
        Hashtable<String, Object> env = new Hashtable<String, Object>();

        Security.addProvider(new Provider());

        Element assertion = getAssertionFromSTS("UserA", "PassA");

        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        env.put("java.naming.provider.url", "remote://localhost:4447");
        env.put("jboss.naming.client.ejb.context", "true");
        env.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        env.put("javax.security.sasl.policy.noplaintext", "false");

        env.put(Context.SECURITY_PRINCIPAL, "admin");
        env.put(Context.SECURITY_CREDENTIALS, DocumentUtil.getNodeAsString(assertion));

        Context context = new InitialContext(env);

        EchoService object = (EchoService) context.lookup("ejb-test/EchoServiceImpl!org.picketlink.test.trust.ejb.EchoService");

        Assert.assertEquals("Hi UserA", object.echo("Hi "));
    }
    
    @Test (expected=EJBAccessException.class)
    public void testNotAuthorizedEJBInvocation() throws Exception {
        Hashtable<String, Object> env = new Hashtable<String, Object>();

        Security.addProvider(new Provider());

        Element assertion = getAssertionFromSTS("UserA", "PassA");

        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        env.put("java.naming.provider.url", "remote://localhost:4447");
        env.put("jboss.naming.client.ejb.context", "true");
        env.put("jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");
        env.put("javax.security.sasl.policy.noplaintext", "false");

        env.put(Context.SECURITY_PRINCIPAL, "admin");
        env.put(Context.SECURITY_CREDENTIALS, DocumentUtil.getNodeAsString(assertion));

        Context context = new InitialContext(env);

        EchoService object = (EchoService) context.lookup("ejb-test/EchoServiceImpl!org.picketlink.test.trust.ejb.EchoService");

        Assert.assertEquals("Hi UserA", object.echoUnchecked("Hi "));
    }

}