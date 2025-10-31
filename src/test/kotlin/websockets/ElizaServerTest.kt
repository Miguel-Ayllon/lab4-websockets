@file:Suppress("NoWildcardImports")

package websockets

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.websocket.ClientEndpoint
import jakarta.websocket.ContainerProvider
import jakarta.websocket.OnMessage
import jakarta.websocket.Session
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ElizaServerTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun onOpen() {
        logger.info { "This is the test worker" }
        val latch = CountDownLatch(3)
        val list = mutableListOf<String>()

        val client = SimpleClient(list, latch)
        client.connect("ws://localhost:$port/eliza")
        latch.await()
        assertEquals(3, list.size)
        assertEquals("The doctor is in.", list[0])
    }

    @Test
    fun onChat() {
        logger.info { "Test thread" }
        val latch = CountDownLatch(4)
        val list = mutableListOf<String>()

        val client = ComplexClient(list, latch)
        client.connect("ws://localhost:$port/eliza")
        latch.await()
        val size = list.size
        
        // 1. EXPLAIN WHY size = list.size IS NECESSARY
        // Capturamos el tamaño de la lista *después* de que el latch se libera.
        // Esto asegura que tenemos el estado final para la aserción.

        // 2. REPLACE BY assertXXX expression that checks an interval; assertEquals must not be used;
        assertTrue(size >= 4) { "Deberíamos recibir al menos 4 mensajes (3 saludos + 1 respuesta)" }

        // 3. EXPLAIN WHY assertEquals CANNOT BE USED AND WHY WE SHOULD CHECK THE INTERVAL
        // No se puede usar assertEquals(4, size) porque las pruebas de WebSocket son asíncronas
        // y pueden ser inestables (brittle). El servidor podría enviar mensajes adicionales
        // después de que el latch(4) se haya liberado.
        // Solo nos importa que *como mínimo* hayamos recibido los 4 mensajes esperados.

        // 4. COMPLETE assertEquals(XXX, list[XXX])
        assertEquals("The doctor is in.", list[0])
        assertEquals("What's on your mind?", list[1])
        assertEquals("---", list[2])
        val response = list[3]
        assertTrue(
            response.contains("you", ignoreCase = true) && response.contains("sad", ignoreCase = true)
        ) { "La respuesta del doctor '$response' debería reflejar 'you' y 'sad'" }
    }
}

@ClientEndpoint
class SimpleClient(
    private val list: MutableList<String>,
    private val latch: CountDownLatch,
) {
    @OnMessage
    fun onMessage(message: String) {
        logger.info { "Client received: $message" }
        list.add(message)
        latch.countDown()
    }
}

@ClientEndpoint
class ComplexClient(
    private val list: MutableList<String>,
    private val latch: CountDownLatch,
) {
    @OnMessage
    fun onMessage(
        message: String,
        session: Session,
    ) {
        logger.info { "Client received: $message" }
        list.add(message)
        latch.countDown()

        // 5. COMPLETE if (expression) {
        if (message.contains("What's on your mind?")) {
            // 6. COMPLETE   sentence
            session.asyncRemote.sendText("I am feeling sad")
        }
        logger.info { "Received messages: ${list.size}" }
    }
}

fun Any.connect(uri: String) {
    ContainerProvider.getWebSocketContainer().connectToServer(this, URI(uri))
}