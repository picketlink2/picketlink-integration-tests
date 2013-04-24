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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.config.SAMLConfigParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PicketLinkConfigurationUtil {

    /**
     * <p>
     * Adds a new trusted domain to the <Trust><Domains> list.
     * </p>
     * 
     * @param webArchive
     * @param domain
     */
    public static final void addTrustedDomain(WebArchive webArchive, String domain) {
        final Node picketlink = getPicketLinkConfigNode(webArchive);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName(SAMLConfigParser.DOMAINS));

        element.setTextContent(element.getTextContent() + "," + domain);

        webArchive.delete(picketlink.getPath());

        overridePicketLinkConfig(webArchive, picketlink, document);
    }
    
    /**
     * <p>
     * Adds a new trusted domain to the <Trust><Domains> list.
     * </p>
     * 
     * @param webArchive
     * @param domain
     */
    public static final void changeIdentityURL(WebArchive webArchive, String identityURL) {
        final Node picketlink = getPicketLinkConfigNode(webArchive);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName(SAMLConfigParser.IDENTITY_URL));

        element.setTextContent(identityURL);

        webArchive.delete(picketlink.getPath());

        overridePicketLinkConfig(webArchive, picketlink, document);
    }
    
    /**
     * <p>
     * Adds a new alias to the keystore. Defaults to /WEB-INF/classes/jbid_test_keystore.jks. 
     * </p>
     * 
     * @param sp
     * @param alias
     */
    public static void addKeyStoreAlias(WebArchive sp, String alias) {
        addKeyStoreAlias(sp, "/WEB-INF/classes/jbid_test_keystore.jks", "servercert", "store123", alias);
    }

    /**
     * <p>
     * Adds a new alias to the keystore specified in the <code>jksPath</code> parameter. 
     * </p>
     * 
     * @param sp
     * @param jksPath
     * @param alias
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws KeyStoreException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public static void addKeyStoreAlias(WebArchive sp, String jksPath, String certAlias, String passwd, String alias) {
        final Node keystore = getContent(sp, jksPath);

        char[] password = passwd.toCharArray();
        
        try {
            final KeyStore jks = KeyStoreUtil.getKeyStore(keystore.getAsset().openStream(), password);

            Certificate certificate = jks.getCertificate(certAlias);

            jks.setCertificateEntry(alias, certificate);

            File file = File.createTempFile("tmpjks", "jks");
            file.deleteOnExit();

            FileOutputStream stream = new FileOutputStream(file);

            jks.store(stream, password);

            stream.close();

            final FileInputStream fileInputStream = new FileInputStream(file);
            sp.delete(keystore.getPath());

            sp.add(new Asset() {
                public InputStream openStream() {
                    return fileInputStream;
                }
            }, keystore.getPath());
        } catch (Exception e) {
            throw new RuntimeException("Error while adding a new alias to the keystore.", e);
        }
    }

    /**
     * <p>
     * Adds a new <KeyProvider><ValidatingAlias>.
     * </p>
     * 
     * @param webArchive
     * @param aliasKey
     * @param aliasValue
     */
    public static void addValidatingAlias(WebArchive webArchive, String aliasKey, String aliasValue) {
        final Node picketlink = getPicketLinkConfigNode(webArchive);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName("KeyProvider"));

        if (element != null) {
            Element createElement = document.createElement("ValidatingAlias");

            createElement.setAttribute("Key", aliasKey);
            createElement.setAttribute("Value", aliasValue);

            element.insertBefore(createElement, element.getLastChild());
        }

        overridePicketLinkConfig(webArchive, picketlink, document);
    }
    
    /**
     * <p>
     * Adds a new <KeyProvider><ValidatingAlias> to the configuration file specified in the <code>configFile</code> parameter.
     * </p>
     * 
     * @param webArchive
     * @param configFile
     * @param aliasKey
     * @param aliasValue
     */
    public static void addValidatingAlias(WebArchive webArchive, String configFile, String aliasKey, String aliasValue) {
        final Node picketlink = getContent(webArchive, configFile);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName("KeyProvider"));

        if (element != null) {
            Element createElement = document.createElement("ValidatingAlias");

            createElement.setAttribute("Key", aliasKey);
            createElement.setAttribute("Value", aliasValue);

            element.insertBefore(createElement, element.getLastChild());
        }

        overridePicketLinkConfig(webArchive, picketlink, document);
    }

    /**
     * Add AttributeProvider to picketlink-sts.xml config.
     * Useful when you need pick roles from SAMLIssuingLoginModule later in SAMLRolesLoginModule.
     * 
     * @param webArchive
     * @param configFile
     * @param tokenRoleAttributeName
     */
    public static void addSAML20TokenRoleAttributeProvider(WebArchive webArchive, String configFile, String tokenRoleAttributeName) {
        final Node picketlink = getContent(webArchive, configFile);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName("TokenProviders"));
        NodeList nl = element.getElementsByTagName("TokenProvider");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            if (e.getNodeName().equals("TokenProvider") 
                    && e.getAttribute("ProviderClass").equals("org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider")
                    && e.getAttribute("TokenElement").equals("Assertion")) {
                
                Element prop1 = document.createElement("Property");
                prop1.setAttribute("Key", "AttributeProvider");
                prop1.setAttribute("Value", "org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider");
                Element prop2 = document.createElement("Property");
                prop2.setAttribute("Key", "org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider.tokenRoleAttributeName");
                prop2.setAttribute("Value", tokenRoleAttributeName); // default "Role"
                
                e.appendChild(prop1);
                e.appendChild(prop2);
                break;
            }
        }
        overridePicketLinkConfig(webArchive, picketlink, document);
    }
    
    private static Document getPicketLinkConfigDocument(final Node picketlink) {
        final Document document;

        try {
            document = DocumentUtil.getDocument(picketlink.getAsset().openStream());
        } catch (Exception e) {
            throw new RuntimeException("Error getting picketlink.xml from WebArchive.", e);
        }
        return document;
    }

    private static Node getPicketLinkConfigNode(WebArchive webArchive) {
        return getContent(webArchive, "/WEB-INF/picketlink.xml");
    }

    private static Node getContent(WebArchive webArchive, final String path) {
        Map<ArchivePath, Node> content = webArchive.getContent(new Filter<ArchivePath>() {

            public boolean include(ArchivePath object) {
                return object.get().equals(path);
            }
        });

        return content.values().iterator().next();
    }

    private static void overridePicketLinkConfig(WebArchive webArchive, final Node picketlink, final Document document) {
        webArchive.delete(picketlink.getPath());
        webArchive.add(new Asset() {
            public InputStream openStream() {
                try {
                    return DocumentUtil.getSourceAsStream(new DOMSource(document.getFirstChild()));
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ProcessingException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }, picketlink.getPath());
    }

}
