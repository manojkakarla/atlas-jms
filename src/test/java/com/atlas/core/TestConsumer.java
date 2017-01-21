package com.atlas.core;

import com.atlas.core.dw.MessageConfig;

import java.util.concurrent.CountDownLatch;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConsumer extends MessageConsumer<MessageConfig> {

    private final CountDownLatch latch;
    @Getter
    private MessageConfig data;

    protected TestConsumer(Class<MessageConfig> clazz, CountDownLatch latch) {
        super(clazz);
        this.latch = latch;
    }

    @Override
    public void handle(MessageConfig data) {
        this.data = data;
        log.info("data = " + data);
        latch.countDown();
    }
}
