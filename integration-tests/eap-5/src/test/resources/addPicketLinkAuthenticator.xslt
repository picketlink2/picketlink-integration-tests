<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the SECURITY_DOMAIN picketlink authenticator to war-deployers-jboss-beans.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:bd="urn:jboss:bean-deployer:2.0"
	version="1.0">

	<xsl:output method="xml" indent="yes" />

	<xsl:template match="//bd:property[@name='authenticators']/bd:map/bd:entry[bd:key/text()='SECURITY_DOMAIN']" />

	<xsl:template match="//bd:property[@name='authenticators']/bd:map">
		<map>
			<xsl:copy-of select="@*"/>
            <entry>
               <key>SECURITY_DOMAIN</key>
               <value>org.picketlink.identity.federation.bindings.tomcat.PicketLinkAuthenticator</value>
            </entry>
			<xsl:apply-templates select="*" />
         </map>  
	</xsl:template>

	<!-- Copy everything else. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>