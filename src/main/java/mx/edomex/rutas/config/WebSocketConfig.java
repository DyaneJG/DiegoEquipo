package mx.edomex.rutas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//TODO (TEMPORAL) Mover la anotación @EnableMethodSecurity al archivo SecurityConfig oficial cuando exista
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSocketMessageBroker
@EnableMethodSecurity // TEMPORAL
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Todo lo que el servidor grite por aquí, llegará a los mapas
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-tracking").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws-tracking-sockjs").setAllowedOriginPatterns("*").withSockJS();
    }
}