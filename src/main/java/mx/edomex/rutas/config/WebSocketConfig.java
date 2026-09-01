package mx.edomex.rutas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitamos un canal de transmisión (broker) que empiece con "/topic"
        // Todo lo que el servidor grite por aquí, llegará a los mapas.
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Endpoint puro para tu aplicación Android en Java
        registry.addEndpoint("/ws-tracking").setAllowedOriginPatterns("*");

        // 2. Endpoint con SockJS (ideal si hacen un panel web y el navegador es antiguo)
        registry.addEndpoint("/ws-tracking-sockjs").setAllowedOriginPatterns("*").withSockJS();
    }
}