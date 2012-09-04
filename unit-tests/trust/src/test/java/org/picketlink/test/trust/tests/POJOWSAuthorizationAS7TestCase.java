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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.picketlink.test.integration.util.PicketLinkIntegrationTests;
import org.picketlink.test.integration.util.TargetContainers;

/**
 * A Simple WS Test for POJO WS Authorization using PicketLink
 * 
 * @author Anil Saldhana
 * @since Oct 3, 2010
 */
@RunWith(PicketLinkIntegrationTests.class)
@TargetContainers({"jbas7"})
public class POJOWSAuthorizationAS7TestCase extends AbstractPOJOWSAuthorizationTestCase {

    @Deployment(name = "pojo-test", testable = false)
    @TargetsContainer("jboss")
    public static WebArchive createWSTestDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/pojo-test/WEB-INF/web.xml"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/pojo-test/WEB-INF/jboss-web.xml"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/pojo-test/WEB-INF/jboss-wsse.xml"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/authorize-handlers.xml"), ArchivePaths.create("classes/authorize-handlers.xml"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/org/picketlink/test/trust/ws/POJOBean.class"), ArchivePaths.create("classes/org/picketlink/test/trust/ws/POJOBean.class"));
        archive.addAsManifestResource(new File("../../unit-tests/trust/target/test-classes/jboss-deployment-structure.xml"));
        
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/props/sts-users.properties"), ArchivePaths.create("classes/users.properties"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/props/sts-roles.properties"), ArchivePaths.create("classes/roles.properties"));
        archive.addAsWebInfResource(new File("../../unit-tests/trust/target/test-classes/props/sts-config.properties"), ArchivePaths.create("classes/sts-config.properties"));

        return archive;
    }

}