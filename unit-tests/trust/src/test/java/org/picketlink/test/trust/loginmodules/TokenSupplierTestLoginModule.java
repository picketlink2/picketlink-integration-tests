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
package org.picketlink.test.trust.loginmodules;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.picketlink.trust.jbossws.handler.MapBasedTokenHandler;

/**
 * This login module simulates customer's login module which constructs binary security token and 
 * supply it using JAAS shared map further to JAAS stack.
 * 
 * @author pskopek
 *
 */
public class TokenSupplierTestLoginModule implements LoginModule {

   String testToken = null;
   String testTokenKey = null;
   
   @SuppressWarnings("rawtypes")
   Map sharedState = null;
   
   public void initialize(Subject subject, CallbackHandler callbackHandler,
         Map<String, ?> sharedState, Map<String, ?> options) {
      
      this.sharedState = sharedState;
      
      testTokenKey = (String)options.get(MapBasedTokenHandler.SYS_PROP_TOKEN_KEY);
      if (testTokenKey != null) {
         testToken = (String)options.get(testTokenKey);
      }
      else if ((testTokenKey = System.getProperty(MapBasedTokenHandler.SYS_PROP_TOKEN_KEY)) != null) {
         testToken = (String)options.get(testTokenKey);
      }
      
   }

   @SuppressWarnings("unchecked")
   public boolean login() throws LoginException {

      if (testToken != null) {
         // add token to shared state map
         sharedState.put(testTokenKey, testToken);
      }
      
      return true; 
   }


   public boolean commit() throws LoginException {
      return true;
   }

   public boolean abort() throws LoginException {
      return false;
   }

   public boolean logout() throws LoginException {
      return false;
   }

}
