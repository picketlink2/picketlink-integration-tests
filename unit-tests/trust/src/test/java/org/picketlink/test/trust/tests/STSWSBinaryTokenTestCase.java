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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;
import org.picketlink.test.integration.util.TestUtil;
import org.picketlink.test.trust.ws.TestBean;
import org.picketlink.test.trust.ws.WSTest;
import org.picketlink.test.trust.ws.handlers.TestBinaryHandler;
import org.picketlink.test.trust.ws.http.TestServletRequest;
import org.picketlink.trust.jbossws.handler.BinaryTokenHandler;

/**
 * Unit Test the {@link BinaryTokenHandler}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Apr 5, 2011
 */
@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers ({"jbas5"})
public class STSWSBinaryTokenTestCase {
  
    @Deployment(name = "picketlink-wstest-tests", testable = false)
    @TargetsContainer("jboss")
    public static JavaArchive createWSTestDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("../../unit-tests/trust/target/picketlink-wstest-tests.jar"));
    }
    
    /**
     * This test case does the following. - We set a Test HttpServletRequest on the soap message context. - We then inject the
     * {@link BinaryTokenHandler} as a client side handler. - On the Server Side, we are hitting the {@link TestBean} which is
     * guarded by the {@link TestBinaryHandler}
     * 
     * The WS has no security. The Server side {@link TestBinaryHandler} ensures that the call comes in with a
     * BinarySecurityToken
     * 
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testWSInteraction() throws Exception {
        System.setProperty("binary.http.header", "TEST_HEADER");

        URL wsdl = new URL(TestUtil.getTargetURL("/picketlink-wstest-tests/TestBean?wsdl"));
        QName serviceName = new QName("http://ws.trust.test.picketlink.org/", "TestBeanService");
        Service service = Service.create(wsdl, serviceName);
        WSTest port = service.getPort(new QName("http://ws.trust.test.picketlink.org/", "TestBeanPort"), WSTest.class);

        TestServletRequest request = new TestServletRequest();
        request.addHeader("TEST_HEADER", "ABCDEFGH");

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(MessageContext.SERVLET_REQUEST, request);
        List<Handler> handlers = bp.getBinding().getHandlerChain();

        BinaryTokenHandler binaryTokenHandler = new BinaryTokenHandler();

        handlers.add(binaryTokenHandler);
        bp.getBinding().setHandlerChain(handlers);

        assertEquals("Test", port.echo("Test"));
    }

    /**
     * This test case does the following. - We set a Test HttpServletRequest on the soap message context. - We then inject the
     * {@link BinaryTokenHandler} as a client side handler. - On the Server Side, we are hitting the {@link TestBean} which is
     * guarded by the {@link TestBinaryHandler}
     * 
     * The WS has no security. The Server side {@link TestBinaryHandler} ensures that the call comes in with a
     * BinarySecurityToken
     * 
     * @throws Exception
     */
    @Test
    public void testWSLackOfBinaryHandlerInteraction() throws Exception {
        System.setProperty("binary.http.header", "TEST_HEADER");

        URL wsdl = new URL(TestUtil.getTargetURL("/picketlink-wstest-tests/TestBean?wsdl"));
        QName serviceName = new QName("http://ws.trust.test.picketlink.org/", "TestBeanService");
        Service service = Service.create(wsdl, serviceName);
        WSTest port = service.getPort(new QName("http://ws.trust.test.picketlink.org/", "TestBeanPort"), WSTest.class);

        TestServletRequest request = new TestServletRequest();
        request.addHeader("TEST_HEADER", "ABCDEFGH");

        try {
            port.echo("Test");
            fail("Should have thrown exception as we do not have binary handler injected");
        } catch (Exception e) {
            if (e instanceof WebServiceException) {
                // pass
            } else
                fail("wrong exception:" + e);
        }
    }
}