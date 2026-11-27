package com.fundoo.notes.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerConfigTest {

    @Test
    void testSchedulingIsEnabled() {
        assertTrue(SchedulerConfig.class.isAnnotationPresent(EnableScheduling.class));
    }
}
