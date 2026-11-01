package com.hackerfinder.backend.usuarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackerfinder.backend.usuarios.model.Tarjeta;


@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    public Tarjeta findByIdTarjeta(Long idTarjeta);

    public List<Tarjeta> findByNumeroTarjeta(int numeroTarjeta);
}
