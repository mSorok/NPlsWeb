package de.unijena.cheminf.nplsweb.nplsweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/molimg/**").addResourceLocations("file:" + NPlsWebApplication.IMAGE_DIR);
        super.addResourceHandlers(registry);
    }
}
