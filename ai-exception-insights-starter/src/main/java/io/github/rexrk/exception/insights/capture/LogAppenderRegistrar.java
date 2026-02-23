package io.github.rexrk.exception.insights.capture;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

public class LogAppenderRegistrar implements InitializingBean, DisposableBean {

    private final RingBufferLogAppender appender;

    public LogAppenderRegistrar(RingBufferLogAppender appender) {
        this.appender = appender;
    }

    @Override
    public void afterPropertiesSet() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        appender.setContext(context);
        appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
    }

    @Override
    public void destroy() {
        appender.stop(); // clean detach on app shutdown
    }
}