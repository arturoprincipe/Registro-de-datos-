package com.example.bienvenida.controller;

import com.example.bienvenida.entity.Estudiante;
import com.example.bienvenida.repository.EstudianteRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Controller
public class EstudianteController {

    private final EstudianteRepository estudianteRepository;

    public EstudianteController(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    private static final List<String> CARRERAS = List.of(
        "Computación e Informática",
        "Arquitectura de Plataformas y Servicios de TI",
        "Administración de Empresas",
        "Contabilidad",
        "Enfermería Técnica",
        "Electrotecnia Industrial"
    );

    // CONSULTAR TODOS LOS ESTUDIANTES
    @GetMapping("/estudiantes")
    public String listarEstudiantes(Model model) {

        List<Estudiante> estudiantes = estudianteRepository.findAll(
            Sort.by(Sort.Direction.ASC, "idEstudiante")
        );

        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("totalEstudiantes", estudiantes.size());
        model.addAttribute("carreras", CARRERAS);

        return "estudiantes";
    }

    // ACTUALIZAR ESTUDIANTE
    @PostMapping("/estudiantes/actualizar")
    public String actualizarEstudiante(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaNacimiento,
            @RequestParam String carrera,
            @RequestParam String semestre,
            @RequestParam String hobby,
            RedirectAttributes redirectAttributes) {

        Estudiante estudiante = estudianteRepository.findById(id).orElse(null);

        if (estudiante == null) {
            redirectAttributes.addFlashAttribute(
                "error", "No se encontró el estudiante."
            );
            return "redirect:/estudiantes";
        }

        nombre = nombre.trim();
        hobby = hobby.trim();

        if (nombre.isEmpty() || hobby.isEmpty()
                || fechaNacimiento == null
                || !CARRERAS.contains(carrera)) {

            redirectAttributes.addFlashAttribute(
                "error", "Verifica los datos ingresados."
            );
            return "redirect:/estudiantes";
        }

        int edad = Period.between(
            fechaNacimiento, LocalDate.now()
        ).getYears();

        if (edad < 15 || edad > 100) {
            redirectAttributes.addFlashAttribute(
                "error", "La edad debe estar entre 15 y 100 años."
            );
            return "redirect:/estudiantes";
        }

        if (nombre.length() > 100 || hobby.length() > 50
                || semestre.length() > 20) {
            redirectAttributes.addFlashAttribute(
                "error", "Uno de los campos supera el límite permitido."
            );
            return "redirect:/estudiantes";
        }

        estudiante.setNombre(nombre);
        estudiante.setFechaNacimiento(fechaNacimiento);
        estudiante.setEdad(edad);
        estudiante.setCarrera(carrera);
        estudiante.setSemestre(semestre);
        estudiante.setHobby(hobby);

        estudianteRepository.save(estudiante);

        redirectAttributes.addFlashAttribute(
            "mensaje", "Estudiante actualizado correctamente."
        );

        return "redirect:/estudiantes";
    }

    // ELIMINAR ESTUDIANTE
    @PostMapping("/estudiantes/eliminar")
    public String eliminarEstudiante(
            @RequestParam Long id,
            RedirectAttributes redirectAttributes) {

        if (estudianteRepository.existsById(id)) {
            estudianteRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                "mensaje", "Estudiante eliminado correctamente."
            );
        } else {
            redirectAttributes.addFlashAttribute(
                "error", "No se encontró el estudiante."
            );
        }

        return "redirect:/estudiantes";
    }
}