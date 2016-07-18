/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author Christian Bremer
 *
 */
@Configuration
@AutoConfigureBefore(name = "org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration")
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(SchedulingProperties.class)
public class SchedulingAutoConfiguration  implements AsyncConfigurer, SchedulingConfigurer {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected SchedulingProperties properties;
    
    @PostConstruct
    public void init() {
        // @formatter:off
        log.info("\n"
               + "**********************************************************************\n"
               + "*  Scheduling Auto Configuration                                     *\n"
               + "**********************************************************************\n"
               + "properties = " + properties + "\n"
               + "**********************************************************************");
        // @formatter:on
    }

    @Bean
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(properties.getScheduledThreadPool());
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean(name = {"taskExecutor", "metricsExecutor"})
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getTaskExecutorCorePoolSize());
        executor.setMaxPoolSize(properties.getTaskExecutorMaxPoolSize());
        executor.setQueueCapacity(properties.getTaskExecutorQueueCapacity());
        executor.setThreadNamePrefix(properties.getTaskExecutorThreadNamePrefix());
        executor.initialize();
        return executor;
    }
    
}
