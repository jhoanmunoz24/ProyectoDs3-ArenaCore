package com.arenacore.identity.config;

import com.arenacore.identity.domain.AuditoriaSeguridad;
import com.arenacore.identity.domain.Rol;
import com.arenacore.identity.domain.ServiceClient;
import com.arenacore.identity.domain.Usuario;
import com.arenacore.identity.repository.AuditoriaSeguridadRepository;
import com.arenacore.identity.repository.RolRepository;
import com.arenacore.identity.repository.ServiceClientRepository;
import com.arenacore.identity.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
public class BootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ServiceClientRepository serviceClientRepository;
    private final AuditoriaSeguridadRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${arenacore.bootstrap.admin-email:admin@arenacore.local}")
    private String adminEmail;

    @Value("${arenacore.bootstrap.admin-password:Admin1234!}")
    private String adminPassword;

    @Value("${arenacore.bootstrap.operator-email:operador@arenacore.local}")
    private String operatorEmail;

    @Value("${arenacore.bootstrap.operator-password:Operador1234!}")
    private String operatorPassword;

    @Value("${arenacore.internal-clients.courts-secret:courts-internal-secret-2026}")
    private String courtsSecret;

    @Value("${arenacore.internal-clients.equipment-secret:equipment-internal-secret-2026}")
    private String equipmentSecret;

    @Value("${arenacore.internal-clients.billing-secret:billing-internal-secret-2026}")
    private String billingSecret;

    public BootstrapRunner(
            JdbcTemplate jdbcTemplate,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            ServiceClientRepository serviceClientRepository,
            AuditoriaSeguridadRepository auditoriaRepository,
            PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.serviceClientRepository = serviceClientRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = jdbcTemplate.update(
                "INSERT INTO identity_bootstrap_lock (lock_key, locked_by) VALUES ('INITIAL_BOOTSTRAP', ?) ON CONFLICT (lock_key) DO NOTHING",
                "identity-service"
        );

        syncServiceClient("courts-service", courtsSecret, "Courts Service");
        syncServiceClient("equipment-service", equipmentSecret, "Equipment Service");
        syncServiceClient("billing-service", billingSecret, "Billing Service");

        if (inserted == 0 && usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.info("Bootstrap de usuarios omitido (ya ejecutado previamente)");
            return;
        }

        if (!usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            Rol adminRol = rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                    .orElseThrow(() -> new IllegalStateException("Rol ROLE_ADMINISTRADOR no encontrado"));
            Usuario admin = new Usuario();
            admin.setId(UUID.randomUUID());
            admin.setNombreCompleto("Administrador ArenaCore");
            admin.setEmail(adminEmail);
            admin.setTelefono("3000000001");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setActivo(true);
            admin.setRoles(Set.of(adminRol));
            usuarioRepository.save(admin);

            auditoriaRepository.save(new AuditoriaSeguridad(
                    admin.getId(), admin.getEmail(), "BOOTSTRAP_ADMIN",
                    "Superadministrador inicial creado bajo lock transaccional", "127.0.0.1"
            ));
            log.info("Superadministrador bootstrap creado: {}", adminEmail);
        }

        if (!usuarioRepository.existsByEmailIgnoreCase(operatorEmail)) {
            Rol opRol = rolRepository.findByNombre("ROLE_OPERADOR")
                    .orElseThrow(() -> new IllegalStateException("Rol ROLE_OPERADOR no encontrado"));
            Usuario operador = new Usuario();
            operador.setId(UUID.randomUUID());
            operador.setNombreCompleto("Operador Recepción ArenaCore");
            operador.setEmail(operatorEmail);
            operador.setTelefono("3000000002");
            operador.setPasswordHash(passwordEncoder.encode(operatorPassword));
            operador.setActivo(true);
            operador.setRoles(Set.of(opRol));
            usuarioRepository.save(operador);
            log.info("Operador bootstrap creado: {}", operatorEmail);
        }
    }

    private void syncServiceClient(String clientId, String rawSecret, String serviceName) {
        ServiceClient client = serviceClientRepository.findById(clientId).orElseGet(ServiceClient::new);
        client.setClientId(clientId);
        client.setServiceName(serviceName);
        client.setClientSecretHash(passwordEncoder.encode(rawSecret));
        client.setActivo(true);
        if (client.getCreadoEn() == null) {
            client.setCreadoEn(Instant.now());
        }
        serviceClientRepository.save(client);
    }
}
