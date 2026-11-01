package ar.edu.utn.dds.k3003.eventos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/** El publicador en modo producción: envía en otro hilo, así RabbitMQ nunca demora la respuesta. */
class PublicadorEventosTest {

  @Test
  void enviaEnSegundoPlanoSinBloquearAQuienPublica() throws Exception {
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    CountDownLatch liberar = new CountDownLatch(1);
    // Un broker lento: el envío queda trabado hasta que el test lo libera.
    doAnswer(inv -> liberar.await(5, TimeUnit.SECONDS))
        .when(rabbit).send(anyString(), anyString(), any(Message.class));
    PublicadorEventos publicador = new PublicadorEventos(rabbit, new ObjectMapper(), "donatrack.eventos", false);

    long inicio = System.nanoTime();
    publicador.publicar("INSIGNIA_CREADA", "ins-1", Map.of("id", "ins-1"));
    long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);

    Assertions.assertTrue(ms < 500, "publicar() no espera al broker (tardó " + ms + " ms)");
    liberar.countDown();
    verify(rabbit, timeout(2000))
        .send(eq("donatrack.eventos"), eq("incentivos.insignia.creada"), any(Message.class));
  }

  @Test
  void lasRoutingKeysSiguenElContratoDelGrupo() {
    Map<String, String> contrato = Map.of(
        "INSIGNIA_CREADA", "incentivos.insignia.creada",
        "MISION_CREADA", "incentivos.mision.creada",
        "PROGRESO_DONADOR_ACTUALIZADO", "incentivos.progreso_donador.actualizado",
        "MODULO_RESETEADO", "incentivos.modulo.reseteado");
    contrato.forEach((tipo, clave) ->
        Assertions.assertEquals(clave, EventoIncentivos.nuevo(tipo, "x", Map.of()).routingKey()));
  }

  @Test
  void unaFallaDeRabbitEnSegundoPlanoNoSePropaga() {
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    doAnswer(inv -> { throw new AmqpConnectException(new java.net.ConnectException("caído")); })
        .when(rabbit).send(anyString(), anyString(), any(Message.class));
    PublicadorEventos publicador = new PublicadorEventos(rabbit, new ObjectMapper(), "donatrack.eventos", false);

    Assertions.assertDoesNotThrow(() -> publicador.publicar("MODULO_RESETEADO", "*", Map.of()));
    verify(rabbit, timeout(2000)).send(anyString(), anyString(), any(Message.class));
  }
}
