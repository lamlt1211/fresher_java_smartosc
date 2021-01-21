package com.smartosc.training.configration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
	
	private static final int POOL_SIZE = 5;
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler poolTaskScheduler = new ThreadPoolTaskScheduler();
		poolTaskScheduler.setPoolSize(POOL_SIZE);
		poolTaskScheduler.setThreadNamePrefix("Scheduled-task-pool-");
		poolTaskScheduler.initialize();
		
		taskRegistrar.setTaskScheduler(poolTaskScheduler);
	}

}
