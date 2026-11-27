package com.fundoo.notes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on @Scheduled methods, used to send reminder emails on time.
 *
 * @author Navya Raj
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
