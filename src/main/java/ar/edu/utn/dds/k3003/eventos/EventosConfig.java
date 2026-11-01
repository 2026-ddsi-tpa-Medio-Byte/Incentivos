package ar.edu.utn.dds.k3003.eventos;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Exchange de eventos del grupo (topic, durable). Lo comparten los cuatro módulos. */
@Configuration
public class EventosConfig {

  @Bean
  public TopicExchange eventosExchange(@Value("${eventos.exchange}") String nombre) {
    return new TopicExchange(nombre, true, false);
  }

  /**
   * Declara el exchange al conectarse si todavía no existe. Si ya lo creó otro módulo con otros
   * atributos, el error de declaración se ignora (queda en el log) y se publica igual al existente,
   * en vez de que falle cada envío.
   */
  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    RabbitAdmin admin = new RabbitAdmin(connectionFactory);
    admin.setIgnoreDeclarationExceptions(true);
    return admin;
  }
}
