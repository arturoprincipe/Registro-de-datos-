package com.example.bienvenida.repository;

import com.example.bienvenida.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstudianteRepository
        extends JpaRepository<Estudiante, Long> {
}