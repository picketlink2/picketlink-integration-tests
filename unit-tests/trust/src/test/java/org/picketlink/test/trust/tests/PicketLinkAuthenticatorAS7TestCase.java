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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.AssetUtil;
import org.junit.runner.RunWith;
import org.picketlink.identity.federation.bindings.tomcat.PicketLinkAuthenticator;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;

/**
 * Test the {@link PicketLinkAuthenticator}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Sep 13, 2011
 */
@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers ({"jbas7"})
public class PicketLinkAuthenticatorAS7TestCase extends AbstractPicketLinkAuthenticatorTestCase {
    
    @Deployment(name = "authenticator", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createAuthenticatorDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        
        archive.addAsWebInfResource(getTestFile("as7/WEB-INF/web.xml"));
        archive.addAsWebInfResource(getTestFile("as7/WEB-INF/jboss-web.xml"));
        archive.addAsManifestResource(getTestFile("as7/META-INF/jboss-deployment-structure.xml"));
        
        archive.addAsWebResource(getTestFile("index.jsp"));
        archive.addAsWebResource(getTestFile("error.html"));
        archive.addAsWebResource(getTestFile("login.html"));
        
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/props/sts-users.properties"), ArchivePaths.create("classes/users.properties"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/props/sts-roles.properties"), ArchivePaths.create("classes/roles.properties"));
        
        return archive;
    }
    
}