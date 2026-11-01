package com.hackerfinder.backend.usuarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hackerfinder.backend.usuarios.model.Producto;
import com.hackerfinder.backend.usuarios.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto nuevoProducto() {
        return new Producto(1L, "Mouse", 99.99, 10, "Accesorios", "desc");
    }

    @Test
    void actualizarStock_ShouldUpdate_WhenProductExists() {
        Producto p = nuevoProducto();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.actualizarStock(1L, 50);

        assertEquals(50, actualizado.getStock());
    }

    @Test
    void actualizarStock_ShouldThrow_WhenProductDoesNotExist() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.actualizarStock(99L, 5));
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void crudMethods_ShouldDelegateToRepository() {
        Producto p = nuevoProducto();
        when(productoRepository.findAll()).thenReturn(List.of(p));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.findById(2L)).thenReturn(Optional.empty());
        when(productoRepository.save(p)).thenReturn(p);
        when(productoRepository.findByCategoria("Accesorios")).thenReturn(List.of(p));

        assertEquals(1, productoService.getAllProductos().size());
        assertEquals(p, productoService.findById(1L));
        assertNull(productoService.findById(2L));
        assertEquals(p, productoService.saveProducto(p));
        assertEquals(1, productoService.findByCategoria("Accesorios").size());

        productoService.delete(1L);
        verify(productoRepository).deleteById(1L);
    }
}
