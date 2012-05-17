package org.picketlink.test.integration.pdp;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.picketlink.test.integration.util.MavenArtifactUtil;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;

@RunWith(PicketLinkIntegrationTests.class)
public abstract class AbstractXACMLIntegrationTests {

    @Deployment(name = "pdp", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createSTSDeployment() throws GeneralSecurityException, IOException {
        return MavenArtifactUtil.getQuickstartsMavenArchive("pdp");
    }

}
