package ar.edu.utn.dds.k3003.config;

import ar.edu.utn.dds.k3003.logging.TrazabilidadInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para hacer llamadas HTTP a otros módulos.
 */
@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    // El interceptor propaga X-Trace-Id y loguea cada llamada saliente.
    return builder.additionalInterceptors(new TrazabilidadInterceptor()).build();
  }
}
