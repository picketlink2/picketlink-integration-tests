package org.picketlink.test.integration.sts;

import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addKeyStoreAlias;
import static org.picketlink.test.integration.util.PicketLinkConfigurationUtil.addValidatingAlias;
import static org.picketlink.test.integration.util.TestUtil.getServerAddress;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;

@RunWith(PicketLinkIntegrationTests.class)
public abstract class AbstractWSTrustIntegrationTests {

    @Deployment(name = "picketlink-sts", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSTSDeployment() throws GeneralSecurityException, IOException {
        WebArchive sts = MavenArtifactUtil.getQuickstartsMavenArchive("picketlink-sts");
        
        addValidatingAlias(sts, "/WEB-INF/classes/picketlink.xml", getServerAddress(), getServerAddress());
        addKeyStoreAlias(sts, "/WEB-INF/classes/sts_keystore.jks", "sts", "testpass", getServerAddress());
        
        return sts;
    }

}
