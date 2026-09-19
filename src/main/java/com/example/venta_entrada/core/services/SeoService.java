package com.example.venta_entrada.core.services;

public interface SeoService {

    /**
     * Genera el documento XML del Sitemap compatible con Google y buscadores web.
     * Incluye las páginas estáticas del frontend y todos los eventos activos dinámicamente.
     *
     * @return String con contenido XML válido.
     */
    String generarSitemapXml();

    /**
     * Genera el contenido del archivo robots.txt con directivas de rastreo para crawlers.
     *
     * @return String con directivas robots.txt.
     */
    String generarRobotsTxt();
}
