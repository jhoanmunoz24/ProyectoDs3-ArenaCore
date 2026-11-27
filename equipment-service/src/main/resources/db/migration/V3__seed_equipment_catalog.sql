INSERT INTO categorias_implemento (id, codigo, nombre, descripcion) VALUES
    ('31111111-1111-1111-1111-111111111101', 'BALONES', 'Balones de Fútbol', 'Balones profesionales #5 para grama sintética'),
    ('31111111-1111-1111-1111-111111111102', 'PETOS', 'Juegos de Petos', 'Chalecos deportivos de entrenamiento en sets de 10'),
    ('31111111-1111-1111-1111-111111111103', 'GUANTES', 'Guantes de Arquero', 'Guantes acolchados para portero'),
    ('31111111-1111-1111-1111-111111111104', 'CONOS', 'Conos y Platillos', 'Conos de entrenamiento y demarcación'),
    ('31111111-1111-1111-1111-111111111105', 'ESCALERAS', 'Escaleras de Agilidad', 'Escaleras plásticas de coordinación');

INSERT INTO implementos (id, categoria_id, codigo, nombre, stock_total, stock_disponible, stock_mantenimiento, codigo_tarifa, activo) VALUES
    ('32222222-2222-2222-2222-222222222201', '31111111-1111-1111-1111-111111111101', 'BALON_PRO_5', 'Balón Golty Profesional #5', 12, 12, 0, 'TAR_BALON_PRO_5', TRUE),
    ('32222222-2222-2222-2222-222222222202', '31111111-1111-1111-1111-111111111102', 'JUEGO_PETOS_10', 'Juego de 10 Petos Bicolor', 10, 10, 0, 'TAR_JUEGO_PETOS', TRUE),
    ('32222222-2222-2222-2222-222222222203', '31111111-1111-1111-1111-111111111103', 'GUANTES_ARQUERO', 'Par de Guantes de Arquero Pro', 8, 8, 0, 'TAR_GUANTES_ARQUERO', TRUE),
    ('32222222-2222-2222-2222-222222222204', '31111111-1111-1111-1111-111111111104', 'KIT_ENTRENAMIENTO', 'Kit Conos y Escalera de Entrenamiento', 6, 6, 0, 'TAR_KIT_ENTRENAMIENTO', TRUE);
