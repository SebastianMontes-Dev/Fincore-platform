package com.fincore.platform.infrastructure.report.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.*;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail reporteJobDetail() {
        return JobBuilder.newJob(ReporteJob.class)
                .withIdentity("reporteJob").storeDurably().build();
    }

    @Bean
    public Trigger reporteTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(reporteJobDetail()).withIdentity("reporteTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 1 * ?"))
                .build();
    }
}
