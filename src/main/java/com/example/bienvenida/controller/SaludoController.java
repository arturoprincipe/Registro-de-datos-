package com.example.bienvenida.controller;

import com.example.bienvenida.entity.Estudiante;
import com.example.bienvenida.service.EstudianteService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Controller
public class SaludoController {

    private final EstudianteService estudianteService;

    public SaludoController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    private static final int EDAD_MINIMA = 15;
    private static final int EDAD_MAXIMA = 100;
    private static final int NOMBRE_MAXIMO = 100;
    private static final int HOBBY_MAXIMO = 50;

    private static final String FORMA_NOMBRE = "\\p{L}[\\p{L} ,]*";
    private static final String FORMA_HOBBY = "\\p{L}[\\p{L} ,]*";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> CARRERAS = List.of(
            "Computación e Informática",
            "Arquitectura de Plataformas y Servicios de TI",
            "Administración de Empresas",
            "Contabilidad",
            "Enfermería Técnica",
            "Electrotecnia Industrial"
    );

    private static final List<String> SEMESTRES = List.of(
            "I semestre", "II semestre", "III semestre",
            "IV semestre", "V semestre", "VI semestre"
    );

    @GetMapping("/")
    public String mostrarFormulario(Model model) {
        cargarListas(model);
        model.addAttribute("valorSemestre", "III semestre");
        return "saludo";
    }

    @PostMapping("/bienvenida")
    public String mostrarBienvenida(
            @RequestParam("nombre") String nombre,
            @RequestParam("fechaNacimiento") String fechaNacimiento,
            @RequestParam("carrera") String carrera,
            @RequestParam("semestre") String semestre,
            @RequestParam("hobby") String hobby,
            Model model) {

        LocalDate fecha = leerFecha(fechaNacimiento);

        String error = validarNombre(nombre);

        if (error == null) {
            error = validarFecha(fecha);
        }

        if (error == null) {
            error = validarHobby(hobby);
        }

        if (error != null) {
            cargarListas(model);
            model.addAttribute("error", error);
            model.addAttribute("valorNombre", nombre);
            model.addAttribute("valorFecha", fechaNacimiento);
            model.addAttribute("valorCarrera", carrera);
            model.addAttribute("valorSemestre", semestre);
            model.addAttribute("valorHobby", hobby);
            return "saludo";
        }

        String nombreFormateado = formatearNombre(nombre);
        int edad = Period.between(fecha, LocalDate.now()).getYears();

        // Crear el objeto estudiante
        Estudiante estudiante = new Estudiante(
                nombreFormateado,
                fecha,
                edad,
                carrera,
                semestre,
                hobby.trim()
        );

        // Guardar los datos en PostgreSQL de Neon
        estudianteService.guardar(estudiante);

        // Mostrar los datos en la pantalla
        model.addAttribute("nombre", nombreFormateado);
        model.addAttribute("fechaNacimiento", fecha.format(FORMATO_FECHA));
        model.addAttribute("edad", edad);
        model.addAttribute("carrera", carrera);
        model.addAttribute("semestre", semestre);
        model.addAttribute("hobby", hobby.trim());

        return "saludo";
    }

    @GetMapping("/bienvenida")
    public String volverAlInicio() {
        return "redirect:/";
    }

    private String validarNombre(String nombre) {
        String limpio = nombre.trim();

        if (limpio.isEmpty()) {
            return "El nombre es obligatorio.";
        }

        if (limpio.length() > NOMBRE_MAXIMO) {
            return "El nombre no puede pasar de " + NOMBRE_MAXIMO + " caracteres.";
        }

        if (!limpio.matches(FORMA_NOMBRE)) {
            return "El nombre solo puede tener letras (sin números ni símbolos).";
        }

        return null;
    }

    private String validarFecha(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();

        if (fecha == null) {
            return "La fecha ingresada no es válida.";
        }

        if (!fecha.isBefore(hoy)) {
            return "La fecha de nacimiento no puede ser de hoy ni futura.";
        }

        if (fecha.isAfter(hoy.minusYears(EDAD_MINIMA))) {
            return "Debes tener al menos " + EDAD_MINIMA + " años para registrarte.";
        }

        if (fecha.isBefore(hoy.minusYears(EDAD_MAXIMA))) {
            return "La fecha no es válida: la edad no puede pasar de " + EDAD_MAXIMA + " años.";
        }

        return null;
    }

    private String validarHobby(String hobby) {
        String limpio = hobby.trim();

        if (limpio.isEmpty()) {
            return "El hobby es obligatorio.";
        }

        if (limpio.length() > HOBBY_MAXIMO) {
            return "El hobby no puede pasar de " + HOBBY_MAXIMO + " caracteres.";
        }

        if (!limpio.matches(FORMA_HOBBY)) {
            return "El hobby solo puede tener letras y comas (sin números ni símbolos).";
        }

        return null;
    }

    private LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void cargarListas(Model model) {
        model.addAttribute("carreras", CARRERAS);
        model.addAttribute("semestres", SEMESTRES);
    }

    private String formatearNombre(String texto) {
        StringBuilder resultado = new StringBuilder();

        for (String palabra : texto.trim().toLowerCase(Locale.ROOT).split("\\s+")) {
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1))
                    .append(" ");
        }

        return resultado.toString().trim();
    }
}