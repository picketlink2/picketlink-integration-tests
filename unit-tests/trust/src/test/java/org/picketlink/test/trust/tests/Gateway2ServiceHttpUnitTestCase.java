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

import static org.junit.Assert.*;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.*;
import static org.picketlink.test.integration.util.TestUtil.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.PicketLinkConfigurationUtil;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;
import org.picketlink.test.trust.loginmodules.TokenSupplierTestLoginModule;
import org.picketlink.test.trust.servlet.GatewayServlet;
import org.picketlink.test.trust.servlet.ServiceServlet;

/**
 * Unit test to test scenario with JBWSTokenIssuingLoginModule as gateway which obtains SAML token and stores it in to the JAAS
 * subject. It is later picked by GatewayServlet app and passed in http request as header to another app (service) which will
 * use SAML2STSLoginModule to get the SAML token and locally validate it and grant access to the service app.
 * 
 * @author Peter Skopek: pskopek at redhat dot com
 * @since Aug 29, 2012
 */

@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers({ "eap5" })
public class Gateway2ServiceHttpUnitTestCase extends TrustTestsBase {

    private static final Logger log = Logger.getLogger(Gateway2ServiceHttpUnitTestCase.class);

    @Test
    public void testG2S_http_compressedTokenScenario() throws Exception {
        String encodedURL = java.net.URLEncoder.encode(getTargetURL("/service/incoming"), "UTF-8");
        log.debug("encoded target URL=" + encodedURL);
        assertServiceApp("/gateway/request?action=forward&serviceServerUrl=" + encodedURL + "&compression=true", "UserA",
                "PassA");
    }

    @Test
    public void testCheckGatewayAuth() throws Exception {
        assertGatewayApp("/gateway/request?action=authInfo", "UserA", "PassA");
    }

    private void assertGatewayApp(String appUri, String userName, String password) throws Exception {

        String content = getContentFromApp(appUri, userName, password);

        assertTrue("Request not authenticated.", content.indexOf("GatewayAuthentication=Success") > -1);

        boolean samlCredPresentOnSubject = samlCredentialPresense(content);
        assertTrue("SamlCredential on subject is missing for (" + appUri + ")", samlCredPresentOnSubject);

    }

    private void assertServiceApp(String appUri, String userName, String password) throws Exception {

        String content = getContentFromApp(appUri, userName, password);

        log.debug("Service content=" + content);
        assertTrue("Request not authenticated.", content.indexOf("ServiceAuthentication=Success") > -1);
        assertTrue("Response has to be from ServiceServlet.",
                content.indexOf("ClassName=" + ServiceServlet.class.getName()) > -1);

        boolean samlCredPresentOnSubject = samlCredentialPresense(content);
        assertTrue("SamlCredential on subject is missing for (" + appUri + ")", samlCredPresentOnSubject);

    }

    private boolean samlCredentialPresense(String content) {
        Pattern p = Pattern.compile("[.|\\s]*Credential\\[\\d\\]\\=SamlCredential\\[.*\\]", Pattern.DOTALL);
        Matcher m = p.matcher(content);
        return m.find();
    }

    private String getContentFromApp(String appUri, String userName, String password) throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        String content = null;

        try {
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(getServerAddress(), 8080), // localhost
                    new UsernamePasswordCredentials(userName, password));

            HttpGet httpget = new HttpGet(getTargetURL(appUri));

            log.debug("executing request:" + httpget.getRequestLine());
            HttpResponse response = httpclient.execute(httpget);
            assertEquals("Http response has to finish with 'HTTP/1.1 200 OK'", 200, response.getStatusLine().getStatusCode());

            HttpEntity entity = response.getEntity();
            log.debug("Status line: " + response.getStatusLine());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            content = baos.toString();
            baos.close();

            if (log.isTraceEnabled()) {
                log.trace(content);
            }
            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        return content;

    }

    @Deployment(name = "g2s-http-sec-domains.jar", testable = false, order = 2)
    @TargetsContainer("jboss")
    public static JavaArchive deployTestScenario1() throws IOException {
        JavaArchive ts = ShrinkWrap.create(JavaArchive.class, "g2s-http-sec-domains.jar");
        ts.addClass(TokenSupplierTestLoginModule.class);
        ts.addAsManifestResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/jboss-beans.xml"));
        return ts;
    }

    @Deployment(name = "gateway.war", testable = false, order = 4)
    @TargetsContainer("jboss")
    public static WebArchive deployGatewayApp() throws IOException {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                .loadMetadataFromPom("pom.xml");

        WebArchive war = ShrinkWrap.create(WebArchive.class, "gateway.war");
        war.addClass(GatewayServlet.class);
        war.addAsLibraries(resolver.artifact("org.apache.httpcomponents:httpclient").resolveAsFiles());
        war.addAsWebInfResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/gateway/jboss-web.xml"));
        war.addAsWebInfResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/gateway/web.xml"));
        return war;
    }

    @Deployment(name = "service.war", testable = false, order = 5)
    @TargetsContainer("jboss")
    public static WebArchive deployServiceApp() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "service.war");
        war.addClass(ServiceServlet.class);
        war.addAsWebInfResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/service/jboss-web.xml"));
        war.addAsWebInfResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/service/web.xml"));
        war.addAsWebInfResource(new File(
                "../../unit-tests/trust/target/test-classes/lmtestapp/gateway2service-http/service/context.xml"));
        return war;
    }

    @Deployment(name = "picketlink-sts", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSTSDeployment() throws GeneralSecurityException, IOException {
        WebArchive sts = MavenArtifactUtil.getQuickstartsMavenArchive("picketlink-sts");

        addValidatingAlias(sts, "/WEB-INF/classes/picketlink-sts.xml", getServerAddress(), getServerAddress());
        addKeyStoreAlias(sts, "/WEB-INF/classes/sts_keystore.jks", "sts", "testpass", getServerAddress());
        PicketLinkConfigurationUtil.addSAML20TokenRoleAttributeProvider(sts, "/WEB-INF/classes/picketlink-sts.xml", "Role");

        return sts;
    }

}