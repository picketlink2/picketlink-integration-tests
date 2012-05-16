package org.picketlink.test.integration.saml2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;

@RunWith(Arquillian.class)
public class AbstractSAMLIntegrationTests {

    String IDP_URL = getTargetURL("/idp/");
    static String IDP_SIG_URL = getTargetURL("/idp-sig/");

    String SALES_POST_URL = getTargetURL("/sales-post/");
    String SALES_POST_SIG_URL = getTargetURL("/sales-post-sig/");
    String SALES_POST_VALVE_URL = getTargetURL("/sales-post-valve/");

    String EMPLOYEE_REDIRECT_URL = getTargetURL("/employee/");
    String EMPLOYEE_REDIRECT_SIG_URL = getTargetURL("/employee-sig/");
    String EMPLOYEE_REDIRECT_VALVE_URL = getTargetURL("/employee-redirect-valve/");

    String LOGOUT_URL = "?GLO=true";

    protected static String getTargetURL(String uri) {
        return "http://" + getServerAddress() + ":" + System.getProperty("test.server.port", "28080") + uri;
    }

    private static String getServerAddress() {
        return System.getProperty("test.hosts.bind.address", "localhost");
    }

    @Deployment(name = "idp", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createIDPDeployment() throws ConfigurationException, ProcessingException, ParsingException,
            InterruptedException {
        WebArchive idp = MavenArtifactUtil.getQuickstartsMavenArchive("idp");
        
        PicketLinkConfigurationUtil.addTrustedDomain(idp, getServerAddress());
        
        return idp;
    }

    @Deployment(name = "idp-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createIDPSigDeployment() throws GeneralSecurityException, IOException {
        WebArchive idp = MavenArtifactUtil.getQuickstartsMavenArchive("idp-sig");
        
        PicketLinkConfigurationUtil.addTrustedDomain(idp, getServerAddress());
        PicketLinkConfigurationUtil.addValidatingAlias(idp, getServerAddress(),getServerAddress());
        PicketLinkConfigurationUtil.addKeyStoreAlias(idp, getServerAddress());
        
        return idp;
    }

    @Deployment(name = "sales-saml11", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesSAML11Deployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("sales-saml11");
    }

    @Deployment(name = "sales-post", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesPostDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("sales-post");
    }

    @Deployment(name = "sales-post-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSalesPostSigDeployment() throws GeneralSecurityException, IOException {
        WebArchive sp = MavenArtifactUtil.getQuickstartsMavenArchive("sales-post-sig");
        
        PicketLinkConfigurationUtil.addKeyStoreAlias(sp, getServerAddress());
        
        return sp;
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

    @Deployment(name = "employee-sig", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createEmployeeSigDeployment() throws KeyStoreException, FileNotFoundException, NoSuchAlgorithmException, CertificateException, GeneralSecurityException, IOException {
        WebArchive sp = MavenArtifactUtil.getQuickstartsMavenArchive("employee-sig");
        
        PicketLinkConfigurationUtil.addKeyStoreAlias(sp, getServerAddress());
        
        return sp;
    }

    @Deployment(name = "employee-redirect-valve", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createEmployeeRedirectValvePostDeployment() {
        return MavenArtifactUtil.getQuickstartsMavenArchive("employee-redirect-valve");
    }

    @Deployment(name = "claims", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createClaimsDeployment() {
        return MavenArtifactUtil.getIntegrationMavenArchive("claims");
    }

}
