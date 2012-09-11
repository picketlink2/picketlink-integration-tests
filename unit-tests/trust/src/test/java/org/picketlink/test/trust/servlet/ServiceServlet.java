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

import java.io.IOException;
import java.io.PrintWriter;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.security.SecurityAssociation;

/**
 * Service Application Servlet
 * 
 * @author pskopek@redhat.com
 *
 */

public class ServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter out = resp.getWriter();
        out.println("ServiceAuthentication=Success");
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
    
}
