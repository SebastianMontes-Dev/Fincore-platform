package com.fincore.platform.infrastructure.common.config;

import com.fincore.platform.infrastructure.auth.security.UsuarioActualResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UsuarioActualResolver usuarioActualResolver;

    public WebConfig(UsuarioActualResolver usuarioActualResolver) {
        this.usuarioActualResolver = usuarioActualResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(usuarioActualResolver);
    }
}
