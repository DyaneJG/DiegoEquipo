package mx.edomex.rutas.pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class StripeConfig {
  @Value("${stripe.secret-key}")
  private String stripeSecretKey;
  
  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeSecretKey;
    log.info("stripe configurado correctamente");
  }
}
