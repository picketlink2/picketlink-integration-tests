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

package org.picketlink.test.integration.saml2;

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
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class PicketLinkConfigurationUtil {

    public static final void addTrustedDomain(WebArchive webArchive, String domain) {
        final Node picketlink = getPicketLinkConfigNode(webArchive);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName("Domains"));

        element.setTextContent(element.getTextContent() + "," + domain);

        webArchive.delete(picketlink.getPath());

        overridePicketLinkConfig(webArchive, picketlink, document);
    }

    public static void addKeyStoreAlias(WebArchive sp, String alias) throws GeneralSecurityException, IOException,
            KeyStoreException, FileNotFoundException, NoSuchAlgorithmException, CertificateException {
        Map<ArchivePath, Node> contentKeyStore = sp.getContent(new Filter<ArchivePath>() {

            public boolean include(ArchivePath object) {
                return object.get().equals("/WEB-INF/classes/jbid_test_keystore.jks");
            }
        });

        final Node keystore = contentKeyStore.values().iterator().next();

        char[] password = "store123".toCharArray();

        final KeyStore jks = KeyStoreUtil.getKeyStore(keystore.getAsset().openStream(), password);

        Certificate certificate = jks.getCertificate("servercert");

        jks.setCertificateEntry(alias, certificate);

        File file = new File("/tmp/tmpjks.jks");

        if (file.exists()) {
            file.delete();
        }

        FileOutputStream stream = new FileOutputStream(file);

        jks.store(stream, password);

        stream.close();

        final FileInputStream fileInputStream = new FileInputStream("/tmp/tmpjks.jks");

        sp.delete(keystore.getPath());

        sp.add(new Asset() {
            public InputStream openStream() {
                return fileInputStream;
            }
        }, keystore.getPath());
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
        Map<ArchivePath, Node> content = webArchive.getContent(new Filter<ArchivePath>() {

            public boolean include(ArchivePath object) {
                return object.get().equals("/WEB-INF/picketlink.xml");
            }
        });

        return content.values().iterator().next();
    }

    public static void addValidatingAlias(WebArchive webArchive, String aliasKey, String aliasValue) {
        final Node picketlink = getPicketLinkConfigNode(webArchive);
        final Document document = getPicketLinkConfigDocument(picketlink);

        Element element = DocumentUtil.getElement(document, new QName("KeyProvider"));

        if (element != null) {
            Element createElement = document.createElement("ValidatingAlias");

            createElement.setAttribute("Key", aliasKey);
            createElement.setAttribute("Value", aliasValue);

            element.appendChild(createElement);
        }

        overridePicketLinkConfig(webArchive, picketlink, document);
    }

    private static void overridePicketLinkConfig(WebArchive webArchive, final Node picketlink, final Document document) {
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
