package com.hackerfinder.backend.usuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.hackerfinder.backend.usuarios.repository.ProductoRepository;
import java.util.List;
import com.hackerfinder.backend.usuarios.model.Producto;

@Service
@Transactional
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> findByCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    public void delete(Long id) {
        productoRepository.deleteById(id);
    }

    public Producto actualizarStock(Long idProducto, Integer nuevoStock) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));
        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }
}
