package com.joel.omnicron.core.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.scheduling.support.CronExpression;

public final class ScheduleUtils {
    private ScheduleUtils() {
    }

    public static Instant nextScheduledAt(String cronExpression, Instant from) {
        if (cronExpression == null || cronExpression.isBlank()) {
            return null;
        }

        CronExpression cron = CronExpression.parse(cronExpression);
        ZonedDateTime next = cron.next(ZonedDateTime.ofInstant(from, ZoneOffset.UTC));

        return next != null ? next.toInstant() : null;
    }
}
