package org.argos.file.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArgosFileManagerApplicationTests {

    @Test
    void contextLoads() {
        // Necessary Comment to avoid sonar Issues.
    }

    @Test
    void testMain() {
        assertDoesNotThrow(
                () -> ArgosFileManagerApplication.main(new String[] {}),
                "Application should start without throwing exceptions");
    }
}
