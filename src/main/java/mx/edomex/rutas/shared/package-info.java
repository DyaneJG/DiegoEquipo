/**
 * Infraestructura transversal compartida por todos los dominios:
 * seguridad, manejo de excepciones, auditoría, cache y eventos.
 *
 * REGLA: los demás dominios pueden depender de este paquete,
 * pero shared NUNCA debe depender de ningún dominio de negocio.
 */
package mx.edomex.rutas.shared;
