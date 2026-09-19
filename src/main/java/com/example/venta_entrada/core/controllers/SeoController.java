package com.example.venta_entrada.core.controllers;

import com.example.venta_entrada.core.services.SeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Tag(name = "SEO & Crawlers", description = "Endpoints para indexación en motores de búsqueda (Google, Bing)")
public class SeoController {

    private final SeoService seoService;

    @Operation(summary = "Obtener el sitemap XML dinámico para Google/Bing")
    @GetMapping(value = {"/api/seo/sitemap.xml", "/sitemap.xml"}, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap() {
        String sitemap = seoService.generarSitemapXml();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=" + TimeUnit.HOURS.toSeconds(1))
                .body(sitemap);
    }

    @Operation(summary = "Obtener directivas de rastreo de robots.txt")
    @GetMapping(value = {"/api/seo/robots.txt", "/robots.txt"}, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRobotsTxt() {
        String robots = seoService.generarRobotsTxt();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=" + TimeUnit.DAYS.toSeconds(1))
                .body(robots);
    }
}
