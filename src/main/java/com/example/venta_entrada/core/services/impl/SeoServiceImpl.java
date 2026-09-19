package com.example.venta_entrada.core.services.impl;

import com.example.venta_entrada.core.services.SeoService;
import com.example.venta_entrada.eventos.models.Evento;
import com.example.venta_entrada.eventos.repositories.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeoServiceImpl implements SeoService {

    private final EventoRepository eventoRepository;

    @Value("${app.frontend-url:https://venta-entrada-app-java-front.vercel.app}")
    private String rawFrontendUrl;

    @Value("${app.backend-url:https://ventaentradaappjava-back.onrender.com}")
    private String backendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String resolveBaseFrontendUrl() {
        if (rawFrontendUrl == null || rawFrontendUrl.isBlank()) {
            return "https://venta-entrada-app-java-front.vercel.app";
        }
        String[] urls = rawFrontendUrl.split(",");
        for (String u : urls) {
            String trimmed = u.trim();
            if (trimmed.startsWith("https://")) {
                return trimmed.replaceAll("/+$", "");
            }
        }
        return urls[0].trim().replaceAll("/+$", "");
    }

    private String resolveBaseBackendUrl() {
        if (backendUrl == null || backendUrl.isBlank()) {
            return "https://ventaentradaappjava-back.onrender.com";
        }
        return backendUrl.replaceAll("/+$", "");
    }

    @Override
    @Transactional(readOnly = true)
    public String generarSitemapXml() {
        String baseUrl = resolveBaseFrontendUrl();
        String hoy = LocalDateTime.now().format(DATE_FORMATTER);

        List<Evento> eventos = eventoRepository.findAll();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 1. Home / Página de Inicio
        xml.append("  <url>\n")
           .append("    <loc>").append(baseUrl).append("/</loc>\n")
           .append("    <lastmod>").append(hoy).append("</lastmod>\n")
           .append("    <changefreq>daily</changefreq>\n")
           .append("    <priority>1.0</priority>\n")
           .append("  </url>\n");

        // 2. Catálogo de Eventos
        xml.append("  <url>\n")
           .append("    <loc>").append(baseUrl).append("/eventos</loc>\n")
           .append("    <lastmod>").append(hoy).append("</lastmod>\n")
           .append("    <changefreq>daily</changefreq>\n")
           .append("    <priority>0.9</priority>\n")
           .append("  </url>\n");

        // 3. Contacto / Soporte
        xml.append("  <url>\n")
           .append("    <loc>").append(baseUrl).append("/contacto</loc>\n")
           .append("    <lastmod>").append(hoy).append("</lastmod>\n")
           .append("    <changefreq>monthly</changefreq>\n")
           .append("    <priority>0.5</priority>\n")
           .append("  </url>\n");

        // 4. Cada Evento Activo dinámicamente
        for (Evento evento : eventos) {
            String fechaModificacion = evento.getFechaActualizacion() != null 
                    ? evento.getFechaActualizacion().format(DATE_FORMATTER)
                    : (evento.getFechaCreacion() != null ? evento.getFechaCreacion().format(DATE_FORMATTER) : hoy);

            xml.append("  <url>\n")
               .append("    <loc>").append(baseUrl).append("/eventos/").append(evento.getId()).append("</loc>\n")
               .append("    <lastmod>").append(fechaModificacion).append("</lastmod>\n")
               .append("    <changefreq>weekly</changefreq>\n")
               .append("    <priority>0.8</priority>\n")
               .append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    @Override
    public String generarRobotsTxt() {
        String baseBackend = resolveBaseBackendUrl();
        return "User-agent: *\n" +
               "Allow: /\n" +
               "Disallow: /swagger-ui/\n" +
               "Disallow: /v3/api-docs/\n" +
               "Disallow: /api/auth/\n" +
               "Allow: /api/seo/sitemap.xml\n" +
               "Allow: /sitemap.xml\n" +
               "\n" +
               "Sitemap: " + baseBackend + "/api/seo/sitemap.xml\n";
    }
}
