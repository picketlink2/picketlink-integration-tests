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

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.junit.Test;
import org.picketlink.test.integration.util.TestUtil;
import org.picketlink.test.trust.ws.WSTest;
import org.picketlink.trust.jbossws.SAML2Constants;
import org.picketlink.trust.jbossws.handler.SAML2Handler;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractSTSWSClientTestCase extends TrustTestsBase {

    private static String username = "UserA";
    private static String password = "PassA";

    @SuppressWarnings("rawtypes")
    @Test
    public void testWSInteraction() throws Exception {
        Element assertion = getAssertionFromSTS(username, password);
        
        // Step 2: Stuff the Assertion on the SOAP message context and add the SAML2Handler to client side handlers
        URL wsdl = new URL(TestUtil.getTargetURL("/ws-testbean/WSTestBean?wsdl"));
        QName serviceName = new QName("http://ws.trust.test.picketlink.org/", "WSTestBeanService");
        Service service = Service.create(wsdl, serviceName);
        WSTest port = service.getPort(new QName("http://ws.trust.test.picketlink.org/", "WSTestBeanPort"), WSTest.class);
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(SAML2Constants.SAML2_ASSERTION_PROPERTY, assertion);
        List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SAML2Handler());
        bp.getBinding().setHandlerChain(handlers);

        // Step 3: Access the WS. Exceptions will be thrown anyway.
        assertEquals("Test", port.echo("Test"));
    }
}
