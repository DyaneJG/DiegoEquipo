package mx.edomex.rutas.auth.service;

import mx.edomex.rutas.auth.dto.*;
import mx.edomex.rutas.auth.model.Rol;
import mx.edomex.rutas.auth.model.Usuario;
import mx.edomex.rutas.auth.repository.RolRepository;
import mx.edomex.rutas.auth.repository.UsuarioRepository;
import mx.edomex.rutas.shared.exception.DominioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService,
                        RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DominioException("Ya existe un usuario con ese email", HttpStatus.CONFLICT);
        }

        Rol rol = rolRepository.findByNombre(request.rolNombre())
            .orElseThrow(() -> new DominioException(
                "Rol no válido: " + request.rolNombre(), HttpStatus.BAD_REQUEST));

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);

        return new RegisterResponse(guardado.getId(), guardado.getEmail(), guardado.getRol().getNombre());
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.email())
            .orElseThrow(() -> new DominioException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new DominioException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        try {
            String accessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
            String refreshToken = refreshTokenService.generar(usuario.getEmail());
            return new LoginResponse(accessToken, refreshToken, 900);
        } catch (Exception e) {
            throw new DominioException("Error generando token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public LoginResponse refresh(RefreshRequest request) {
        String email = refreshTokenService.obtenerEmail(request.refreshToken())
            .orElseThrow(() -> new DominioException("Refresh token inválido o expirado", HttpStatus.UNAUTHORIZED));

        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email)
            .orElseThrow(() -> new DominioException("Usuario no encontrado", HttpStatus.UNAUTHORIZED));

        try {
            String nuevoAccessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
            return new LoginResponse(nuevoAccessToken, request.refreshToken(), 900);
        } catch (Exception e) {
            throw new DominioException("Error generando token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
