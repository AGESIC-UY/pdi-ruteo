<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:a="http://www.w3.org/2005/08/addressing">

    <xsl:output omit-xml-declaration="yes"/>

    <xsl:param name="wsaTo"/>

    <!-- Realiza la transformacion identidad -->
    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="a:To">
        <a:To><xsl:value-of select="$wsaTo"/></a:To>
    </xsl:template>
</xsl:stylesheet>