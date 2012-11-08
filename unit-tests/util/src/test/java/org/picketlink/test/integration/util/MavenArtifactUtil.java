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

package org.picketlink.test.integration.util;

import java.util.Map;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * <p>
 * Utility class to retrieve artifacts from the local m2 repository used during the integration tests. 
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class MavenArtifactUtil {

    /**
     * <p>
     * Returns a {@link WebArchive} instance from the org.picketlink.quickstarts groupId (PicketLink Quickstarts Project).
     * </p>
     * 
     * @param artifactId
     * @return
     */
    public static WebArchive getQuickstartsMavenArchive(String artifactId) {
        WebArchive artifact = getMavenArchiveResolver()
                .artifact("org.picketlink.quickstarts:" + artifactId + ":war:" + System.getProperty("binding") + ":" + System.getProperty("version.picketlink.quickstarts")).configureFrom("../../unit-tests/util/src/test/resources/settings.xml").resolveAs(WebArchive.class).iterator().next();
        
        return renameArtifact(artifactId, artifact);
    }
    
    /**
     * <p>
     * Returns a {@link WebArchive} instance from the org.picketlink groupId.
     * </p>
     * 
     * @param artifactId
     * @return
     */
    public static WebArchive getIntegrationMavenArchive(String artifactId) {
        WebArchive artifact = getMavenArchiveResolver()
                .artifact("org.picketlink:" + artifactId + ":war:" + System.getProperty("project.version"))
                .goOffline().resolveAs(WebArchive.class).iterator().next();
        
        return renameArtifact(artifactId, artifact);
    }

    /**
     * <p>
     * Rename a {@link WebArchive} to the value specified in the parameter <code>newName</code>.
     * This method is useful when working with Apache Tomcat, for example, where the deployed file name is used as the application context path.
     * </p>
     * 
     * @param newName
     * @param artifact
     * @return
     */
    private static WebArchive renameArtifact(String newName, WebArchive artifact) {
        WebArchive renamedArtifact = ShrinkWrap.create(WebArchive.class, newName + ".war");
        
        for (Map.Entry<ArchivePath, Node> content : artifact.getContent().entrySet()) {
            if (content.getValue().getAsset() != null) {
                renamedArtifact.add(content.getValue().getAsset(), content.getKey());
            }
        }
        return renamedArtifact;
    }

    private static MavenDependencyResolver getMavenArchiveResolver() {
        return DependencyResolvers.use(MavenDependencyResolver.class);
    }

}
