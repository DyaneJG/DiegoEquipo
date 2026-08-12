package mx.edomex.rutas.notificaciones.service.impl;

import mx.edomex.rutas.notificaciones.DTO.CorreoRequestDTO;
import mx.edomex.rutas.notificaciones.model.Notificacion;
import mx.edomex.rutas.notificaciones.repository.NotificacionRepository;
import mx.edomex.rutas.notificaciones.service.NotificacionService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final JavaMailSender mailSender;//libreria para mandar emails
    private final NotificacionRepository notificacionRepository;

    // Inyección de dependencias
    public NotificacionServiceImpl(JavaMailSender mailSender, NotificacionRepository notificacionRepository) {
        this.mailSender = mailSender;
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    public void enviarCorreo(CorreoRequestDTO request) {
        // 1. Preparamos el registro para la base de datos
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(request.usuarioId());
        notificacion.setPedidoId(request.pedidoId());
        notificacion.setCanal("email");
        notificacion.setTitulo(request.titulo());
        notificacion.setCuerpo(request.cuerpo());
        notificacion.setEstado("pendiente");

        try {
            // se construye el correo
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(request.correoDestino());
            mensaje.setSubject(request.titulo());
            mensaje.setText(request.cuerpo());

            // se envia el correo
            mailSender.send(mensaje);

            //Si no hubo errores, marcamos como enviado
            notificacion.setEstado("enviado");
            notificacion.setEnviadoAt(LocalDateTime.now());

        } catch (Exception e) {
            // Si algo falla 
            notificacion.setEstado("fallido");
            notificacion.setErrorDetalle(e.getMessage());
        } finally {
            //Guardamos el historial en la BD pase lo que pase
            notificacionRepository.save(notificacion);
        }
    }
}