/**
 * 
 */
package org.bremersee.common.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("b2c.scheduling")
public class SchedulingProperties {
    
    private int scheduledThreadPool = 100;
    
    private int taskExecutorCorePoolSize = 100;

    private int taskExecutorMaxPoolSize = Integer.MAX_VALUE;

    private int taskExecutorQueueCapacity = Integer.MAX_VALUE;

    private int taskExecutorKeepAliveSeconds = 60;

    private String taskExecutorThreadNamePrefix = "b2cTaskExecutor-";

    @Override
    public String toString() {
        // @formatter:off
        return "SchedulingProperties ["
                + "\n- scheduledThreadPool=" + scheduledThreadPool 
                + ", \n- taskExecutorCorePoolSize=" + taskExecutorCorePoolSize 
                + ", \n- taskExecutorMaxPoolSize=" + taskExecutorMaxPoolSize 
                + ", \n- taskExecutorQueueCapacity=" + taskExecutorQueueCapacity 
                + ", \n- taskExecutorKeepAliveSeconds=" + taskExecutorKeepAliveSeconds
                + ", \n- taskExecutorThreadNamePrefix=" + taskExecutorThreadNamePrefix 
                + "]";
        // @formatter:on
    }

    public int getScheduledThreadPool() {
        return scheduledThreadPool;
    }

    public void setScheduledThreadPool(int scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }

    public int getTaskExecutorCorePoolSize() {
        return taskExecutorCorePoolSize;
    }

    public void setTaskExecutorCorePoolSize(int taskExecutorCorePoolSize) {
        this.taskExecutorCorePoolSize = taskExecutorCorePoolSize;
    }

    public int getTaskExecutorMaxPoolSize() {
        return taskExecutorMaxPoolSize;
    }

    public void setTaskExecutorMaxPoolSize(int taskExecutorMaxPoolSize) {
        this.taskExecutorMaxPoolSize = taskExecutorMaxPoolSize;
    }

    public int getTaskExecutorQueueCapacity() {
        return taskExecutorQueueCapacity;
    }

    public void setTaskExecutorQueueCapacity(int taskExecutorQueueCapacity) {
        this.taskExecutorQueueCapacity = taskExecutorQueueCapacity;
    }

    public int getTaskExecutorKeepAliveSeconds() {
        return taskExecutorKeepAliveSeconds;
    }

    public void setTaskExecutorKeepAliveSeconds(int taskExecutorKeepAliveSeconds) {
        this.taskExecutorKeepAliveSeconds = taskExecutorKeepAliveSeconds;
    }

    public String getTaskExecutorThreadNamePrefix() {
        return taskExecutorThreadNamePrefix;
    }

    public void setTaskExecutorThreadNamePrefix(
            String taskExecutorThreadNamePrefix) {
        this.taskExecutorThreadNamePrefix = taskExecutorThreadNamePrefix;
    }

}
