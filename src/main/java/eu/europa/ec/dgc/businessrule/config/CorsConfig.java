package eu.europa.ec.dgc.businessrule.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final DgcConfigProperties dgcConfigProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(dgcConfigProperties.getAllowedCorsUrls()).allowedMethods("*");
    }

}
