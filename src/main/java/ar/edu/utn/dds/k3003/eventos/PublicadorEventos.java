package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.logging.Trazabilidad;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publica los eventos de Incentivos en el exchange topic de eventos del grupo (el mismo RabbitMQ de
 * Logística, nunca su cola de trabajo).
 *
 * <p>Dos reglas de la consigna:
 *
 * <ul>
 *   <li><b>Después de guardar.</b> Si hay una transacción en curso, el envío se registra para
 *       después del commit: un cambio que termina en rollback nunca se anuncia.
 *   <li><b>Publicar nunca hace fallar la operación.</b> El envío corre en un hilo aparte y cualquier
 *       error queda en un WARN: si RabbitMQ no responde, la respuesta al usuario no cambia ni se
 *       demora. Un solo hilo mantiene el orden en que ocurrieron los cambios.
 * </ul>
 */
@Component
public class PublicadorEventos {

  private static final Logger log = LoggerFactory.getLogger(PublicadorEventos.class);

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final String exchange;
  private final Executor envios;

  public PublicadorEventos(
      RabbitTemplate rabbitTemplate,
      ObjectMapper objectMapper,
      @Value("${eventos.exchange}") String exchange,
      @Value("${eventos.envio-sincronico:false}") boolean envioSincronico) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
    this.exchange = exchange;
    // En los tests se envía en el mismo hilo para poder verificar el mensaje enseguida.
    this.envios = envioSincronico
        ? Runnable::run
        : Executors.newSingleThreadExecutor(r -> {
            Thread hilo = new Thread(r, "eventos-incentivos");
            hilo.setDaemon(true);
            return hilo;
          });
  }

  public void publicar(String tipo, String entidadId, Object datos) {
    EventoIncentivos evento = EventoIncentivos.nuevo(tipo, entidadId, datos);
    String traceId = MDC.get(Trazabilidad.MDC_TRACE_ID);
    Runnable envio = () -> envios.execute(() -> enviar(evento, traceId));
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          envio.run();
        }
      });
    } else {
      envio.run();
    }
  }

  private void enviar(EventoIncentivos evento, String traceId) {
    if (traceId != null) {
      MDC.put(Trazabilidad.MDC_TRACE_ID, traceId);
    }
    try {
      MessageProperties propiedades = new MessageProperties();
      propiedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
      propiedades.setContentEncoding("UTF-8");
      propiedades.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
      propiedades.setMessageId(evento.eventoId());
      propiedades.setType(evento.tipo());
      if (traceId != null) {
        propiedades.setHeader(Trazabilidad.HEADER_TRACE, traceId);
      }
      Message mensaje = new Message(objectMapper.writeValueAsBytes(evento), propiedades);
      rabbitTemplate.send(exchange, evento.routingKey(), mensaje);
      log.info("Evento {} publicado (entidadId={}, eventoId={})",
          evento.tipo(), evento.entidadId(), evento.eventoId());
    } catch (Exception e) {
      log.warn("No se pudo publicar el evento {} (entidadId={}, eventoId={}): {}",
          evento.tipo(), evento.entidadId(), evento.eventoId(), e.getMessage());
    } finally {
      MDC.remove(Trazabilidad.MDC_TRACE_ID);
    }
  }
}
