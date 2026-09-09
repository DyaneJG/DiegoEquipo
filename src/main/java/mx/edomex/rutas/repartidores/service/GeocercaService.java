package mx.edomex.rutas.repartidores.service;

import org.springframework.stereotype.Service;

@Service
public class GeocercaService {

    // Radio de la Tierra en metros
    private static final double RADIO_TIERRA_METROS = 6371000.0;

    /**
     * Calcula la distancia en metros entre dos coordenadas GPS usando la fórmula de Haversine.
     */
    public double calcularDistanciaEnMetros(double latitudOrigen, double longitudOrigen,
                                            double latitudDestino, double longitudDestino) {

        // 1. Convertimos las coordenadas de grados a radianes
        double lat1Rad = Math.toRadians(latitudOrigen);
        double lat2Rad = Math.toRadians(latitudDestino);
        double deltaLat = Math.toRadians(latitudDestino - latitudOrigen);
        double deltaLon = Math.toRadians(longitudDestino - longitudOrigen);

        // 2. Aplicamos la matemática de Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 3. Multiplicamos por el radio de la Tierra para sacar los metros reales
        return RADIO_TIERRA_METROS * c;
    }

    /**
     * Método lógico para determinar el estado del paquete basadonos en la distancia
     */
    public String determinarEstadoPorDistancia(double distanciaMetros) {
        if (distanciaMetros <= 100.0) {
            return "LLEGANDO_AL_DOMICILIO"; // A menos de 100 metros, ya está en la calle del cliente
        } else if (distanciaMetros <= 1500.0) {
            return "EN_TRANSITO_CERCANO"; // A kilómetro y medio
        } else {
            return "EN_CAMINO"; // Muy lejos aún
        }
    }
}