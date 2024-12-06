package org.argos.file.manager.config;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    public static final String X_REQUESTED_WITH = "X-Requested-With";

    @Bean
    public CorsFilter corsFilter() {
        var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(
                List.of(System.getenv("ArgosAPI_address"), System.getenv("ArgosUI_address")));
        corsConfiguration.setAllowedHeaders(
                List.of(
                        ORIGIN,
                        ACCESS_CONTROL_ALLOW_ORIGIN,
                        CONTENT_TYPE,
                        ACCEPT,
                        AUTHORIZATION,
                        X_REQUESTED_WITH,
                        ACCESS_CONTROL_REQUEST_METHOD,
                        ACCESS_CONTROL_REQUEST_HEADERS,
                        ACCESS_CONTROL_ALLOW_CREDENTIALS));
        corsConfiguration.setExposedHeaders(
                List.of(
                        ORIGIN,
                        ACCESS_CONTROL_ALLOW_ORIGIN,
                        CONTENT_TYPE,
                        ACCEPT,
                        AUTHORIZATION,
                        X_REQUESTED_WITH,
                        ACCESS_CONTROL_REQUEST_METHOD,
                        ACCESS_CONTROL_REQUEST_HEADERS,
                        ACCESS_CONTROL_ALLOW_CREDENTIALS));
        corsConfiguration.setAllowedMethods(
                List.of(
                        GET.name(),
                        POST.name(),
                        PUT.name(),
                        PATCH.name(),
                        DELETE.name(),
                        OPTIONS.name()));
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
