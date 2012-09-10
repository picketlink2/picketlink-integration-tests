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

package org.picketlink.test.trust.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;

/**
 * Gateway Application Servlet
 * 
 * @author pskopek@redhat.com
 *
 */

public class GatewayServlet extends HttpServlet {

    public static Logger log = Logger.getLogger(GatewayServlet.class.getName());
    
    private static final long serialVersionUID = 1L;
    public static final String ACTION_PARAM = "action";
    public static final String AUTH_INFO = "authInfo";
    public static final String FORWARD_CALL = "forward";
    public static final String TOKEN_COMPRESSION_PARAM = "compress";
    public static final String SERVICE_SERVER_URL = "serviceServerUrl";
    
    public static final String AUTH_HEADER = "Auth"; // same as defined in service.war/WEB-INF/context.xml
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("GatewayServlet:doGet");
        String actionParam = req.getParameter(ACTION_PARAM);
        if (actionParam == null || actionParam.equals(AUTH_INFO)) {
            authInfoAction(req, resp);
        }
        else if (actionParam.equals(FORWARD_CALL)) {
            forwardCall(req, resp);
        }
        else {
            PrintWriter out = resp.getWriter();
            String unknownAction = "Action: " + ACTION_PARAM + "=" + actionParam + " unknown.";
            out.println(unknownAction);
            throw new ServletException(unknownAction);
        }
    }
    
    
    private void authInfoAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        out.println("GatewayAuthentication=Success");
        out.println("ClassName="+this.getClass().getName());
        out.println("--------------------------------------");

        out.println("UserPrincipal="+req.getUserPrincipal().getName());
        out.println("--------------------------------------");
     
        Subject subject = SecurityAssociation.getSubject();
        if (subject != null) {
            out.println("Subject="+subject);
            out.println("--------------------------------------");
            out.println("PublicCredentialsSize="+subject.getPublicCredentials().size());
            out.println("--------------------------------------");
            int i = 0;
            for (Object c: subject.getPublicCredentials()) {
                out.println("Credential[" + i + "]=" + c.toString());
                i++;
            }
        }
    }
    
    private void forwardCall(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String serviceServerUrl = req.getParameter(SERVICE_SERVER_URL);
        if (serviceServerUrl == null) {
            serviceServerUrl="http://localhost:8080/service/incoming";
        }
        log.debug("serviceServerUrl="+serviceServerUrl);

        String sCompress = req.getParameter(TOKEN_COMPRESSION_PARAM);
        boolean compress = Boolean.parseBoolean(sCompress);
        log.debug("compress="+compress);
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpget = new HttpGet(serviceServerUrl);
            String samlToken = getSamlToken().getAssertionAsString();
            log.debug("samlToken from subject="+samlToken);
            int options = Base64.NO_OPTIONS;
            if (compress) {
                options = Base64.GZIP;
            }
            
            String encodedToken = Base64.encodeBytes(samlToken.getBytes(), options | Base64.DONT_BREAK_LINES);
            log.debug("encodedToken="+encodedToken);
            
            httpget.addHeader(AUTH_HEADER, "token=\"" + encodedToken + "\"");
            HttpResponse response = httpclient.execute(httpget);
            log.debug("Httpget: " + httpget.toString());
            log.debug("Response: " + response.getStatusLine());
            HttpEntity entity = response.getEntity();

            if (entity.getContentLength() > 0) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entity.getContentLength());
                entity.writeTo(baos);
                baos.close();
                resp.getOutputStream().write(baos.toByteArray());
            }
            else {
                resp.getWriter().println("No response from " + serviceServerUrl);
            }
   
        }
        finally {
            httpclient.getConnectionManager().shutdown();
        }

    }
    
    private SamlCredential getSamlToken() {
        Subject subject = SecurityAssociation.getSubject();
        for (Object c: subject.getPublicCredentials()) {
            if (c instanceof SamlCredential)
                return (SamlCredential)c;
        }
        return null;
    }
    
}
