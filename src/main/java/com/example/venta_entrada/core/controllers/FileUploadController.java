package com.example.venta_entrada.core.controllers;

import com.example.venta_entrada.core.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/upload")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Slf4j
public class FileUploadController {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/webp", "image/jpg"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Autowired
    private CloudinaryService cloudinaryService;
     
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No se ha seleccionado ningún archivo"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "El archivo excede el tamaño máximo permitido de 10MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Formato no permitido. Solo se aceptan imágenes JPG, PNG o WEBP"));
            }
            
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Collections.singletonMap("url", imageUrl));
        } catch (Exception e) {
            log.error("Error al subir imagen a Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error interno al procesar y subir la imagen"));
        }
    }
}
