package com.atlas.core;

import com.atlas.core.config.GatewayConfig;
import com.atlas.core.config.Router;
import com.atlas.core.config.SpringJmsConfig;
import com.atlas.core.dw.AtlasApp;
import com.atlas.core.dw.AtlasJmsApp;
import com.atlas.infrastructure.SpringBundle;
import io.dropwizard.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AtlasJmsAppBuilder<T extends Configuration> extends AtlasAppBuilder<T> {

    public AtlasJmsAppBuilder<T> withJms() {
        return withJms(false, null, null);
    }

    public AtlasJmsAppBuilder<T> withJms(boolean traceEnabled) {
        return withJms(traceEnabled, null, null);
    }

    public AtlasJmsAppBuilder<T> withJms(String header, Map<String, Class<? extends MessageConsumer>> routeMap) {
        return withJms(false, header, routeMap);
    }

    public AtlasJmsAppBuilder<T> withJms(boolean traceEnabled, String header,
                                         Map<String, Class<? extends MessageConsumer>> routeMap) {
        Router router = new Router(header, routeMap);
        router.setTraceEnabled(traceEnabled);
        getBeans().put("router", router);
        getSpringConfigs().add(SpringJmsConfig.class);
        return this;
    }

    public AtlasJmsAppBuilder<T> withGateway() {
        getSpringConfigs().add(GatewayConfig.class);
        return this;
    }

    @Override
    protected <A extends AtlasApp<T>> A buildDefaultApp(String name, SpringBundle<T> springBundle) {
        return (A) new AtlasJmsApp(name, springBundle);
    }

    public AtlasJmsApp<T> build() {
        return build(AtlasJmsApp.class);
    }
}
