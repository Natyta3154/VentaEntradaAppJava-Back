package com.example.venta_entrada.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de Caché en Memoria de Alto Rendimiento utilizando Caffeine.
 * 
 * Permite reducir drásticamente el tiempo de respuesta de la API (TTFB) y evitar
 * consultas repetitivas a la base de datos MySQL en los endpoints públicos de eventos y catálogo.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_EVENTOS = "eventos";
    public static final String CACHE_EVENTO_DETALLE = "evento_detalle";
    public static final String CACHE_ARTISTAS_EVENTO = "artistas_evento";
    public static final String CACHE_TIPOS_ENTRADA = "tipos_entrada_evento";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_EVENTOS,
                CACHE_EVENTO_DETALLE,
                CACHE_ARTISTAS_EVENTO,
                CACHE_TIPOS_ENTRADA
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
        );

        return cacheManager;
    }
}
