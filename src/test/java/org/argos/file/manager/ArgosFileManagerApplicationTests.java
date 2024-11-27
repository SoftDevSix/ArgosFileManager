package org.argos.file.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ArgosFileManagerApplicationTests {

    @Test
    void contextLoads() {
        // Este método está vacío intencionadamente.
        // Se utiliza para verificar que el contexto de la aplicación se carga correctamente.
    }

    @Test
    void testMain() {
        assertDoesNotThrow(() -> ArgosFileManagerApplication.main(new String[]{}),
                "Application should start without throwing exceptions");
    }
}

