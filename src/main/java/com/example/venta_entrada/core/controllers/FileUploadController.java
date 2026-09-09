package com.example.venta_entrada.core.controllers;

import com.example.venta_entrada.core.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class FileUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;
     
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No file selected"));
            }
            
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Collections.singletonMap("url", imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error uploading image: " + e.getMessage()));
        }
    }
}
