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
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.integration.util.TestUtil.getServerAddress;
import static org.picketlink.test.integration.util.TestUtil.getTargetURL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.logging.Logger;
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
import org.picketlink.test.trust.loginmodules.TokenSupplierTestLoginModule;
import org.picketlink.test.trust.servlet.TestAppServlet;
import org.picketlink.trust.jbossws.jaas.JBWSTokenIssuingLoginModule;

/**
 * Unit test the {@link JBWSTokenIssuingLoginModule}
 *
 * @author Anil.Saldhana@redhat.com
 * @author pskopek@redhat.com
 * @since Apr 25, 2011
 */

@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers({ "jbas5", "eap5" })
public class JBWSTokenIssuingLoginModuleUnitTestCase extends TrustTestsBase {

    private static final Logger log = Logger.getLogger(JBWSTokenIssuingLoginModule.class);

    @Test
    public void testLMusingBinaryTokenHandler() throws Exception {
        assertApp("/test-app-2/test", "UserA", "PassA", "TEST_HEADER", "supper-secret-binary-token");
    }

    @Test
    public void testLMusingMapBasedTokenHandler() throws Exception {
        assertApp("/test-app-1/test", "UserA", "PassA", null, null);
    }

    private void assertApp(String appUri, String userName, String password, String httpHeader, String httpHeaderValue)
            throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(getServerAddress(), 8080), // localhost
                    new UsernamePasswordCredentials(userName, password));

            HttpGet httpget = new HttpGet(getTargetURL(appUri)); // http://localhost:8080/test-app-1/test
            if (httpHeader != null)
                httpget.addHeader(httpHeader, httpHeaderValue);

            log.debug("executing request" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            assertEquals("Http response has to finish with 'HTTP/1.1 200 OK'", 200, response.getStatusLine().getStatusCode());

            HttpEntity entity = response.getEntity();
            log.debug("Status line: " + response.getStatusLine());

            ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entity.getContentLength());
            entity.writeTo(baos);
            String content = baos.toString();
            baos.close();

            if (log.isTraceEnabled()) {
                log.trace(content);
            }

            Pattern p = Pattern.compile("[.|\\s]*Credential\\[\\d\\]\\=SamlCredential\\[.*\\]", Pattern.DOTALL);
            Matcher m = p.matcher(content);
            boolean samlCredPresentOnSubject = m.find();
            assertTrue("SamlCredential on subject is missing for (" + appUri + ")", samlCredPresentOnSubject);

            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }

    }

    @Deployment(name = "test-scenario-1.jar", testable = false, order = 2)
    @TargetsContainer("jboss")
    public static JavaArchive deployTestScenario1() throws IOException {
        JavaArchive ts = ShrinkWrap.create(JavaArchive.class);
        ts.addClass(TokenSupplierTestLoginModule.class);
        ts.addAsManifestResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/test-app-1/jboss-beans.xml"));
        // ts.as(ZipExporter.class).exportTo(new File("test-scenario-1.jar"), true);
        return ts;
    }

    @Deployment(name = "test-scenario-2.jar", testable = false, order = 3)
    @TargetsContainer("jboss")
    public static JavaArchive deployTestScenario2() throws IOException {
        JavaArchive ts = ShrinkWrap.create(JavaArchive.class);
        ts.addClass(TokenSupplierTestLoginModule.class);
        ts.addAsManifestResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/test-app-2/jboss-beans.xml"));
        return ts;
    }

    @Deployment(name = "test-app-1.war", testable = false, order = 4)
    @TargetsContainer("jboss")
    public static WebArchive deployTestApp1() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(TestAppServlet.class);
        war.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/test-app-1/jboss-web.xml"));
        war.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/web.xml"));
        // war.as(ZipExporter.class).exportTo(new File("test-app-1.war"), true);
        return war;
    }

    @Deployment(name = "test-app-2.war", testable = false, order = 5)
    @TargetsContainer("jboss")
    public static WebArchive deployTestApp2() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(TestAppServlet.class);
        war.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/test-app-2/jboss-web.xml"));
        war.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/lmtestapp/web.xml"));
        // war.as(ZipExporter.class).exportTo(new File("test-app-2.war"), true);
        return war;
    }

    // just to override
    public static JavaArchive createWSTestDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        return null;
    }

}