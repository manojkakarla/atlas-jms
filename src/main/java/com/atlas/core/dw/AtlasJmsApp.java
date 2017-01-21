package com.atlas.core.dw;

import com.atlas.core.config.Router;
import com.atlas.infrastructure.SpringBundle;
import io.dropwizard.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Arrays;

@Slf4j
public class AtlasJmsApp<T extends Configuration> extends AtlasApp<T> {

    public AtlasJmsApp(String name, SpringBundle<T> bundle) {
        super(name, bundle);
    }

    @Override
    protected void doAdditionalBindings() {
        String[] routers = context.getBeanNamesForType(Router.class);
        if (routers.length != 1) {
            throw new NoSuchBeanDefinitionException(
                    String.format("Wanted a bean for class: %s but found: %s", Router.class.getName(), Arrays.toString(routers)));
        }
        Router router = context.getBean(Router.class);
        router.addRoutes(context);
    }
}
