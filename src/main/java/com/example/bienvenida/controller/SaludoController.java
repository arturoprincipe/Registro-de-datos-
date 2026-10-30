package com.example.bienvenida.controller;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controlador: recibe el formulario, valida los datos y decide qué pantalla mostrar.
 * Usa una sola plantilla (saludo.html): sin "nombre" muestra el registro, con "nombre" los datos.
 */
@Controller
public class SaludoController {

    // Reglas del sistema (se cambian solo aquí)
    private static final int EDAD_MINIMA = 15;
    private static final int EDAD_MAXIMA = 100;
    private static final int NOMBRE_MAXIMO = 100;
    private static final int HOBBY_MAXIMO = 50;

    // \p{L} = cualquier letra (con tildes y ñ); el texto debe empezar con una letra
    private static final String FORMA_NOMBRE = "\\p{L}[\\p{L} ,]*";   // letras, espacios y coma
    private static final String FORMA_HOBBY = "\\p{L}[\\p{L} ,]*";    // letras, espacios y coma

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> CARRERAS = List.of(
            "Computación e Informática",
            "Arquitectura de Plataformas y Servicios de TI",
            "Administración de Empresas",
            "Contabilidad",
            "Enfermería Técnica",
            "Electrotecnia Industrial");

    private static final List<String> SEMESTRES = List.of(
            "I semestre", "II semestre", "III semestre",
            "IV semestre", "V semestre", "VI semestre");

    // Pantalla 1: formulario  ->  http://localhost:8091/
    @GetMapping("/")
    public String mostrarFormulario(Model model) {
        cargarListas(model);
        model.addAttribute("valorSemestre", "III semestre");
        return "saludo";
    }

    // Pantalla 2: se ejecuta al presionar "Generar bienvenida"
    @PostMapping("/bienvenida")
    public String mostrarBienvenida(
            @RequestParam("nombre") String nombre,
            @RequestParam("fechaNacimiento") String fechaNacimiento,
            @RequestParam("carrera") String carrera,
            @RequestParam("semestre") String semestre,
            @RequestParam("hobby") String hobby,
            Model model) {

        // Valida en el orden del formulario y muestra solo el primer error
        LocalDate fecha = leerFecha(fechaNacimiento);
        String error = validarNombre(nombre);
        if (error == null) {
            error = validarFecha(fecha);
        }
        if (error == null) {
            error = validarHobby(hobby);
        }

        // Si hay error, vuelve al formulario conservando lo que ya se escribió
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

        // Todo correcto: envía los datos a la pantalla de resultado
        model.addAttribute("nombre", formatearNombre(nombre));
        model.addAttribute("fechaNacimiento", fecha.format(FORMATO_FECHA));
        model.addAttribute("edad", Period.between(fecha, LocalDate.now()).getYears());
        model.addAttribute("carrera", carrera);
        model.addAttribute("semestre", semestre);
        model.addAttribute("hobby", hobby.trim());   // el hobby se muestra tal como se escribió
        return "saludo";
    }

    // Si recargan /bienvenida con GET, vuelven al formulario
    @GetMapping("/bienvenida")
    public String volverAlInicio() {
        return "redirect:/";
    }

    // Validaciones: devuelven el mensaje de error, o null si todo está bien

    private String validarNombre(String nombre) {
        String limpio = nombre.trim();

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

        if (limpio.length() > HOBBY_MAXIMO) {
            return "El hobby no puede pasar de " + HOBBY_MAXIMO + " caracteres.";
        }
        if (!limpio.matches(FORMA_HOBBY)) {
            return "El hobby solo puede tener letras y comas (sin números ni símbolos).";
        }
        return null;
    }

    // Convierte el texto del calendario en fecha (null si no es válida)
    private LocalDate leerFecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Envía las opciones de las listas desplegables
    private void cargarListas(Model model) {
        model.addAttribute("carreras", CARRERAS);
        model.addAttribute("semestres", SEMESTRES);
    }

    // "luiz sanches jose" -> "Luiz Sanches Jose"
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
