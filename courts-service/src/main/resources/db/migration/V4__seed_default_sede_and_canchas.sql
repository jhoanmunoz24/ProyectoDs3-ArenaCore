INSERT INTO sedes (id, nombre, direccion, telefono, hora_apertura, hora_cierre, zona_horaria, activo) VALUES
    ('21111111-1111-1111-1111-111111111101', 'Sede Principal ArenaCore', 'Calle 5 # 45-20, Cali, Valle del Cauca', '6023001000', '07:00:00', '23:00:00', 'America/Bogota', TRUE);

INSERT INTO canchas (id, sede_id, nombre, tipo, capacidad_jugadores, superficie, tiene_iluminacion, estado_operativo) VALUES
    ('22222222-2222-2222-2222-222222222201', '21111111-1111-1111-1111-111111111101', 'Bombonera Fútbol 5 - Norte', 'FUTBOL_5', 10, 'GRAMA_SINTETICA_FIFA', TRUE, 'HABILITADA'),
    ('22222222-2222-2222-2222-222222222202', '21111111-1111-1111-1111-111111111101', 'Maracaná Fútbol 7 - Centro', 'FUTBOL_7', 14, 'GRAMA_SINTETICA_FIFA', TRUE, 'HABILITADA'),
    ('22222222-2222-2222-2222-222222222203', '21111111-1111-1111-1111-111111111101', 'Camp Nou Fútbol 8 - Sur', 'FUTBOL_8', 16, 'GRAMA_SINTETICA_FIFA', TRUE, 'HABILITADA'),
    ('22222222-2222-2222-2222-222222222204', '21111111-1111-1111-1111-111111111101', 'Estadio Central Fútbol 11', 'FUTBOL_11', 22, 'GRAMA_SINTETICA_FIFA', TRUE, 'HABILITADA');
