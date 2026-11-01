package com.hackerfinder.backend.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hackerfinder.backend.usuarios.model.Producto;
import com.hackerfinder.backend.usuarios.service.ProductoService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private Producto producto;

    private static final String PRODUCTO_JSON =
            "{\"nombre\":\"Mouse\", \"precio\":99.99, \"stock\":10, \"categoria\":\"Accesorios\", \"descripcion\":\"Mouse ergonómico\"}";

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Mouse", 99.99, 10, "Accesorios", "Mouse ergonómico");
    }

    @Test
    void crearProducto_ShouldReturnCreated() throws Exception {
        when(productoService.saveProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCTO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.nombre").value("Mouse"));
    }

    @Test
    void crearProducto_ShouldReturn500_WhenServiceFails() throws Exception {
        when(productoService.saveProducto(any(Producto.class))).thenThrow(new RuntimeException("BD caída"));

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCTO_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAll_ShouldReturnList_OrNoContent() throws Exception {
        when(productoService.getAllProductos()).thenReturn(List.of(producto));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Mouse"));

        when(productoService.getAllProductos()).thenReturn(List.of());
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findById_ShouldReturnProduct_OrNotFound() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByCategoria_ShouldReturnList_OrNoContent() throws Exception {
        when(productoService.findByCategoria("Accesorios")).thenReturn(List.of(producto));
        when(productoService.findByCategoria("Vacia")).thenReturn(List.of());

        mockMvc.perform(get("/api/productos/categoria/Accesorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Accesorios"));
        mockMvc.perform(get("/api/productos/categoria/Vacia"))
                .andExpect(status().isNoContent());
    }
}
