package com.atlas.core;


import org.apache.camel.Handler;

public abstract class MessageConsumer<T> {

    private final Class<T> clazz;

    protected MessageConsumer(Class<T> clazz){
        this.clazz = clazz;
    }

    @Handler
    public abstract void handle(T data);

    @SuppressWarnings("unchecked")
    public Class<T> getHandledType() {
        return clazz;

    }

}
