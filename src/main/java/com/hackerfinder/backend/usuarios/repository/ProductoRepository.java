package com.hackerfinder.backend.usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.hackerfinder.backend.usuarios.model.Producto;
import java.util.List;


@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    public Producto findByIdProducto(Long idProducto);
    
    public List<Producto> findByCategoria(String categoria);

    @Query(value = "SELECT * FROM producto WHERE stock > 0 ORDER BY nombre ASC", nativeQuery = true)
    public List<Producto> obtenerProductosDisponibles();

    @Query(value = "SELECT * FROM producto ORDER BY precio DESC LIMIT 10", nativeQuery = true)
    public List<Producto> obtenerTop10ProductosPorPrecio();
    
    @Query("""
        SELECT p FROM Producto p
        WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
        ORDER BY p.nombre ASC
    """)
    public List<Producto> buscarPorTexto(@Param("texto") String texto);


}
