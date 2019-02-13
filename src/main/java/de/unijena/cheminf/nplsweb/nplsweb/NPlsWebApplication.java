package de.unijena.cheminf.nplsweb.nplsweb;

import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageProperties;
import de.unijena.cheminf.nplsweb.nplsweb.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class NPlsWebApplication {
    static String IMAGE_DIR;


    public static void main(String[] args) {
        IMAGE_DIR = "./molimg/";
        SpringApplication.run(NPlsWebApplication.class, args);
    }



    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
