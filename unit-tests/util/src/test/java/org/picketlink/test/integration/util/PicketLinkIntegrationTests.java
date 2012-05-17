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

import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class PicketLinkIntegrationTests extends Arquillian {

    private final Logger logger = Logger.getLogger(getClass().getName());
    
    public PicketLinkIntegrationTests(Class<?> klass) throws InitializationError {
        super(klass);
    }
    
    /* (non-Javadoc)
     * @see org.jboss.arquillian.junit.Arquillian#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier) {
        TargetContainers targetContainers = getDescription().getAnnotation(TargetContainers.class);
        String currentModule = getCurrentBinding();
        boolean isSupported = false;

        if (targetContainers != null) {
            String[] bindings = targetContainers.value();
            
            for (String binding : bindings) {
                if (currentModule.contains(binding)) {
                    if (getForcedBindings() != null) {
                        String[] forces = getForcedBindings().split(",");
                        
                        for (String string : forces) {
                            if (currentModule.contains(string)) {
                                isSupported = true;
                            }
                        }
                    } else {
                        isSupported = true;    
                    }
                }
            }
        }
        
        if (isSupported) {
            super.run(notifier);
        } else {
            logger.info("Test class " + getTestClass().getName() + " will be ignored for binding " + currentModule);
        }
    }

    private String getCurrentBinding() {
        return System.getProperty("project.artifactId");
    }
    
    private String getForcedBindings() {
        return System.getProperty("forceBinding");
    }
}