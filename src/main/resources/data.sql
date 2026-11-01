-- Insertar usuarios de ejemplo
INSERT INTO usuario (username, nombre_real, email, contrasena) VALUES
('juan_dev', 'Juan García', 'juan@example.com', 'password123'),
('maria_hacker', 'María López', 'maria@example.com', 'pass456'),
('carlos_pro', 'Carlos Martínez', 'carlos@example.com', 'securepass'),
('ana_expert', 'Ana Rodríguez', 'ana@example.com', 'expertpass'),
('luis_newbie', 'Luis Pérez', 'luis@example.com', 'newbiepass');

-- Insertar productos de ejemplo
INSERT INTO producto (nombre, precio, stock, categoria, descripcion) VALUES
('Laptop Dell XPS 13', 1299.99, 15, 'Computadoras', 'Laptop ultradelgada de alta performance con procesador Intel i7 y 16GB RAM'),
('Monitor LG 27 UltraWide', 499.99, 8, 'Monitores', 'Monitor ultrawide 27 pulgadas con resolución 3440x1440 y panel IPS'),
('Teclado Mecánico Corsair', 189.99, 25, 'Accesorios', 'Teclado mecánico RGB con switches Cherry MX y respuesta ultra rápida'),
('Mouse Logitech MX Master', 99.99, 32, 'Accesorios', 'Mouse ergonómico profesional con precisión de 8K DPI'),
('Monitor Samsung 32 4K', 799.99, 5, 'Monitores', 'Monitor gaming 32 pulgadas 4K con 144Hz y tiempo de respuesta 1ms'),
('SSD Samsung 970 EVO 1TB', 149.99, 45, 'Almacenamiento', 'Unidad SSD NVMe de última generación con velocidades de hasta 3500 MB/s'),
('Webcam Logitech 4K', 129.99, 18, 'Accesorios', 'Cámara web 4K con enfoque automático y micrófono estéreo integrado'),
('Auriculares Sony WH-1000XM5', 379.99, 12, 'Audio', 'Auriculares inalámbricos premium con cancelación de ruido activa'),
('Cable USB-C 2m', 19.99, 100, 'Cables', 'Cable USB-C de alta velocidad soporta carga rápida y transferencia de datos'),
('Pasta Térmica Thermal Grizzly', 14.99, 50, 'Componentes', 'Pasta térmica de alta conductividad para refrigeración de procesadores');
