<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:s="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
                xmlns:a="http://www.w3.org/2005/08/addressing"
                xmlns:e="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output omit-xml-declaration="yes"/>
    <!-- Realiza la transformacion identidad -->
    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Setea el valor del atributo mustUnderstand a 0 al elemento de security del Header -->
    <xsl:template match="s:Security/@e:mustUnderstand">
        <xsl:attribute name="e:mustUnderstand">0</xsl:attribute>
    </xsl:template>

    <!-- Setea el valor del atributo mustUnderstand a 0 de todos los elementos de addressing que posean el atributo mustUnderstand -->
    <xsl:template match="a:*[@e:mustUnderstand]/@e:mustUnderstand">
        <xsl:attribute name="e:mustUnderstand">0</xsl:attribute>
    </xsl:template>
</xsl:stylesheet>