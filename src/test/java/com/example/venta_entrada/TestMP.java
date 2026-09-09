package com.example.venta_entrada;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Este test exponía tokens reales y llamadas externas a MP. Ha sido deshabilitado por seguridad.")
public class TestMP {
    
    @Test
    void testMercadoPagoIntegration() {
        // Implementar un test con Mock de PreferenceClient
    }
}
