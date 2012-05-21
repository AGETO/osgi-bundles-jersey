/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.server.impl.application;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.core.spi.component.ComponentProvider;
import com.sun.jersey.core.spi.component.ProviderFactory;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class DeferredResourceConfig extends DefaultResourceConfig {

    private static final Logger LOGGER =
            Logger.getLogger(DeferredResourceConfig.class.getName());

    private final Class<? extends Application> appClass;

    private final Set<Class<?>> defaultClasses;
    
    public DeferredResourceConfig(Class<? extends Application> appClass) {
        this(appClass, Collections.<Class<?>>emptySet());
    }

    public DeferredResourceConfig(Class<? extends Application> appClass, 
            Set<Class<?>> defaultClasses) {
        this.appClass = appClass;
        this.defaultClasses = defaultClasses;
    }

    public ApplicationHolder getApplication(ProviderFactory pf) {
        return new ApplicationHolder(pf);
    }

    public class ApplicationHolder {
        private final Application originalApp;

        private final DefaultResourceConfig adaptedApp;

        private ApplicationHolder(ProviderFactory pf) {
            final ComponentProvider cp = pf.getComponentProvider(appClass);
            if (cp == null) {
                throw new ContainerException("The Application class " + appClass.getName() + " could not be instantiated");
            }
            this.originalApp = (Application)cp.getInstance();

            if ((originalApp.getClasses() == null || originalApp.getClasses().isEmpty()) &&
                    (originalApp.getSingletons() == null || originalApp.getSingletons().isEmpty())) {
                LOGGER.info("Instantiated the Application class " + appClass.getName() +
                        ". The following root resource and provider classes are registered: " + defaultClasses);
                this.adaptedApp = new DefaultResourceConfig(defaultClasses);
                adaptedApp.add(originalApp);
            } else {
                LOGGER.info("Instantiated the Application class " + appClass.getName());
                adaptedApp = null;
            }

        }

        public Application getOriginalApplication() {
            return originalApp;
        }

        public Application getApplication() {
            return (adaptedApp != null) ? adaptedApp : originalApp;
        }
    }
}