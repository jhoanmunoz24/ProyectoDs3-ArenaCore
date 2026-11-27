package com.arenacore.identity.service;

import com.arenacore.identity.config.JwtTokenService;
import com.arenacore.identity.domain.AuditoriaSeguridad;
import com.arenacore.identity.domain.AutorizacionExcepcional;
import com.arenacore.identity.domain.Rol;
import com.arenacore.identity.domain.ServiceClient;
import com.arenacore.identity.domain.Usuario;
import com.arenacore.identity.dto.IdentityDtos.*;
import com.arenacore.identity.repository.AuditoriaSeguridadRepository;
import com.arenacore.identity.repository.AutorizacionExcepcionalRepository;
import com.arenacore.identity.repository.RolRepository;
import com.arenacore.identity.repository.ServiceClientRepository;
import com.arenacore.identity.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class IdentityManagementService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ServiceClientRepository serviceClientRepository;
    private final AutorizacionExcepcionalRepository autorizacionRepository;
    private final AuditoriaSeguridadRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public IdentityManagementService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            ServiceClientRepository serviceClientRepository,
            AutorizacionExcepcionalRepository autorizacionRepository,
            AuditoriaSeguridadRepository auditoriaRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.serviceClientRepository = serviceClientRepository;
        this.autorizacionRepository = autorizacionRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipOrigen) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email())
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            auditoriaRepository.save(new AuditoriaSeguridad(
                    usuario != null ? usuario.getId() : null,
                    request.email(),
                    "LOGIN_FALLIDO",
                    "Credenciales inválidas",
                    ipOrigen
            ));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            auditoriaRepository.save(new AuditoriaSeguridad(
                    usuario.getId(),
                    usuario.getEmail(),
                    "LOGIN_FALLIDO",
                    "Usuario inactivo intentó iniciar sesión",
                    ipOrigen
            ));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario se encuentra inactivo");
        }

        String token = jwtTokenService.generateUserToken(usuario);
        auditoriaRepository.save(new AuditoriaSeguridad(
                usuario.getId(),
                usuario.getEmail(),
                "LOGIN_EXITOSO",
                "Token JWT RS256 emitido correctamente",
                ipOrigen
        ));

        return new AuthResponse(token, "Bearer", jwtTokenService.getUserExpirationSeconds(), toUserResponse(usuario));
    }

    @Transactional
    public AuthResponse registerClient(RegisterRequest request, String ipOrigen) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese correo");
        }
        Rol clienteRol = rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol ROLE_CLIENTE no configurado"));

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setTelefono(request.telefono());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setActivo(true);
        usuario.setRoles(Set.of(clienteRol));
        usuarioRepository.save(usuario);

        auditoriaRepository.save(new AuditoriaSeguridad(
                usuario.getId(), usuario.getEmail(), "USUARIO_CREADO",
                "Registro público de cliente/capitán", ipOrigen
        ));

        String token = jwtTokenService.generateUserToken(usuario);
        return new AuthResponse(token, "Bearer", jwtTokenService.getUserExpirationSeconds(), toUserResponse(usuario));
    }

    @Transactional
    public ServiceTokenResponse issueServiceToken(ServiceTokenRequest request, String ipOrigen) {
        ServiceClient client = serviceClientRepository.findById(request.clientId())
                .filter(ServiceClient::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente de servicio no autorizado"));

        if (!passwordEncoder.matches(request.clientSecret(), client.getClientSecretHash())) {
            auditoriaRepository.save(new AuditoriaSeguridad(
                    null, request.clientId(), "SERVICE_TOKEN_RECHAZADO",
                    "Secreto inválido para token técnico SERVICE_INTERNAL", ipOrigen
            ));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales de servicio inválidas");
        }

        String token = jwtTokenService.generateServiceToken(client);
        auditoriaRepository.save(new AuditoriaSeguridad(
                null, client.getClientId(), "SERVICE_TOKEN_EMITIDO",
                "Token SERVICE_INTERNAL emitido para " + client.getServiceName(), ipOrigen
        ));

        return new ServiceTokenResponse(token, "Bearer", jwtTokenService.getServiceExpirationSeconds(), client.getServiceName());
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request, UUID actorId, String ipOrigen) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con el correo " + request.email());
        }

        Set<Rol> roles = resolveRoles(request.roles());
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setTelefono(request.telefono());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setActivo(true);
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        auditoriaRepository.save(new AuditoriaSeguridad(
                actorId, usuario.getEmail(), "USUARIO_CREADO",
                "Usuario creado por administrador con roles: " + roles.stream().map(Rol::getNombre).toList(),
                ipOrigen
        ));
        return toUserResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return usuarioRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toUserResponse(usuario);
    }

    @Transactional
    public UserResponse assignRoles(UUID userId, RoleAssignmentRequest request, UUID actorId, String ipOrigen) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Set<Rol> roles = resolveRoles(request.roles());
        usuario.setRoles(roles);
        usuario.setActualizadoEn(Instant.now());
        usuarioRepository.save(usuario);

        auditoriaRepository.save(new AuditoriaSeguridad(
                actorId, usuario.getEmail(), "ROLES_ACTUALIZADOS",
                "Nuevos roles asignados: " + roles.stream().map(Rol::getNombre).toList(),
                ipOrigen
        ));
        return toUserResponse(usuario);
    }

    @Transactional
    public UserResponse updateUserStatus(UUID userId, UserStatusRequest request, UUID actorId, String ipOrigen) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setActivo(request.activo());
        usuario.setActualizadoEn(Instant.now());
        usuarioRepository.save(usuario);

        auditoriaRepository.save(new AuditoriaSeguridad(
                actorId, usuario.getEmail(), "ESTADO_USUARIO_ACTUALIZADO",
                "Estado activo cambiado a: " + request.activo(), ipOrigen
        ));
        return toUserResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return rolRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getNombre(), r.getDescripcion()))
                .toList();
    }

    @Transactional
    public AutorizacionResponse createAutorizacion(AutorizacionCreateRequest request, UUID solicitanteId, String ipOrigen) {
        AutorizacionExcepcional auth = new AutorizacionExcepcional();
        auth.setId(UUID.randomUUID());
        auth.setTipo(request.tipo().trim().toUpperCase());
        auth.setReferenciaExternaId(request.referenciaExternaId());
        auth.setSolicitadoPorId(solicitanteId);
        auth.setMotivo(request.motivo().trim());
        auth.setEstado("PENDIENTE");
        autorizacionRepository.save(auth);

        auditoriaRepository.save(new AuditoriaSeguridad(
                solicitanteId, null, "AUTORIZACION_SOLICITADA",
                "Solicitud de autorización excepcional " + auth.getId() + " tipo " + auth.getTipo(), ipOrigen
        ));
        return toAutorizacionResponse(auth);
    }

    @Transactional
    public AutorizacionResponse resolveAutorizacion(UUID id, boolean aprobar, String observacion, UUID adminId, String ipOrigen) {
        AutorizacionExcepcional auth = autorizacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autorización excepcional no encontrada"));
        if (!"PENDIENTE".equals(auth.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La autorización ya fue resuelta previamente");
        }
        auth.setEstado(aprobar ? "APROBADA" : "RECHAZADA");
        auth.setAprobadoPorId(adminId);
        auth.setObservacionResolucion(observacion);
        auth.setResueltoEn(Instant.now());
        autorizacionRepository.save(auth);

        auditoriaRepository.save(new AuditoriaSeguridad(
                adminId, null, aprobar ? "AUTORIZACION_APROBADA" : "AUTORIZACION_RECHAZADA",
                "Autorización " + id + " marcada como " + auth.getEstado(), ipOrigen
        ));
        return toAutorizacionResponse(auth);
    }

    @Transactional(readOnly = true)
    public List<AutorizacionResponse> listAutorizaciones(String estado) {
        List<AutorizacionExcepcional> items = (estado == null || estado.isBlank())
                ? autorizacionRepository.findAll()
                : autorizacionRepository.findByEstadoOrderByCreadoEnDesc(estado.toUpperCase());
        return items.stream().map(this::toAutorizacionResponse).toList();
    }

    @Transactional(readOnly = true)
    public AutorizacionResponse getAutorizacion(UUID id) {
        AutorizacionExcepcional auth = autorizacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autorización excepcional no encontrada"));
        return toAutorizacionResponse(auth);
    }

    private Set<Rol> resolveRoles(List<String> roleNames) {
        Set<Rol> roles = new HashSet<>();
        for (String raw : roleNames) {
            String normalized = normalizeRoleName(raw);
            Rol rol = rolRepository.findByNombre(normalized)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + raw));
            roles.add(rol);
        }
        return roles;
    }

    private String normalizeRoleName(String raw) {
        String upper = raw.trim().toUpperCase();
        return upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
    }

    private UserResponse toUserResponse(Usuario u) {
        return new UserResponse(
                u.getId(),
                u.getNombreCompleto(),
                u.getEmail(),
                u.getTelefono(),
                u.isActivo(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
                u.getCreadoEn()
        );
    }

    private AutorizacionResponse toAutorizacionResponse(AutorizacionExcepcional a) {
        return new AutorizacionResponse(
                a.getId(), a.getTipo(), a.getReferenciaExternaId(),
                a.getSolicitadoPorId(), a.getAprobadoPorId(), a.getMotivo(),
                a.getEstado(), a.getObservacionResolucion(), a.getCreadoEn(), a.getResueltoEn()
        );
    }
}
