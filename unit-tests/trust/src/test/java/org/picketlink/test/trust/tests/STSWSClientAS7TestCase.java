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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.picketlink.test.integration.util.TargetContainers;

/**
 * A Simple WS Test for the SAML Profile of WSS
 * 
 * @author Marcus Moyses
 * @author Anil Saldhana
 * @since Oct 3, 2010
 */
@TargetContainers ({"jbas7"})
public class STSWSClientAS7TestCase extends AbstractSTSWSClientTestCase {
    
    @Deployment(name = "ws-testbean", testable = false)
    @TargetsContainer("jboss")
    public static JavaArchive createWSTestDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "ws-testbean.jar");
        
        archive.addAsResource(new File("../../unit-tests/trust/target/test-classes/handlers.xml"));
        archive.addAsResource(new File("../../unit-tests/trust/target/test-classes/org/picketlink/test/trust/ws/WSTestBean.class"), ArchivePaths.create("org/picketlink/test/trust/ws/WSTestBean.class"));
        archive.addAsManifestResource(new File("../../unit-tests/trust/target/test-classes/jboss-deployment-structure.xml"));
        
        archive.addAsResource(new File("../../unit-tests/trust/target/test-classes/props/sts-users.properties"), ArchivePaths.create("users.properties"));
        archive.addAsResource(new File("../../unit-tests/trust/target/test-classes/props/sts-roles.properties"), ArchivePaths.create("roles.properties"));
        archive.addAsResource(new File("../../unit-tests/trust/target/test-classes/props/sts-config.properties"), ArchivePaths.create("sts-config.properties"));
        
        return archive;
    }
    
}