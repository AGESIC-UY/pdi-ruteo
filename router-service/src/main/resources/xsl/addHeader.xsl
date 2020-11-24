<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:a="http://www.w3.org/2005/08/addressing"
                xmlns:e='http://schemas.xmlsoap.org/soap/envelope/'>

    <xsl:output omit-xml-declaration="yes" method="xml"/>

    <xsl:param name="action"/>

    <xsl:template match="e:Envelope">
        <xsl:copy>
            <xsl:if test="local-name(/e:Envelope/child::*[1]) != 'Header'">
                <xsl:element name="Header" namespace="http://schemas.xmlsoap.org/soap/envelope/">
                    <a:Action><xsl:value-of select="$action"/>Response
                    </a:Action>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Realiza la transformacion identidad -->
    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="e:Header">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:if test="count(/e:Envelope/child::*[1][local-name() = 'Header']/a:Action) = 0">
                <a:Action><xsl:value-of select="$action"/>Response
                </a:Action>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
    
