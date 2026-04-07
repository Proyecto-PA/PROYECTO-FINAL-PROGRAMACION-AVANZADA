package co.edu.uniquindio.gestion_solicitudes.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ia")
public class IaProperties {
    private Boolean habilitada;
    private Api api;

    @Getter
    @Setter
    public static class Api {
        private String url;
        private String key;
        private String model;
    }
}
