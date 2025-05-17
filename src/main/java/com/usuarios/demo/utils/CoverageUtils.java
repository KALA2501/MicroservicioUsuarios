package com.usuarios.demo.utils;

public class CoverageUtils {

    public static String evaluarEdad(int edad) {
        if (edad < 0) {
            return "Edad inválida";
        } else if (edad < 18) {
            return "Menor de edad";
        } else if (edad < 60) {
            return "Adulto";
        } else {
            return "Adulto mayor";
        }
    }

        public static boolean esNumeroPar(int numero) {
        return numero % 2 == 0;
    }

    public static String clasificarIMC(double imc) {
        if (imc < 18.5) {
            return "Bajo peso";
        } else if (imc < 25) {
            return "Normal";
        } else if (imc < 30) {
            return "Sobrepeso";
        } else {
            return "Obesidad";
        }
    }

    public static boolean contieneSoloLetras(String texto) {
        return texto != null && texto.matches("[a-zA-Z]+");
    }

    public static String formatearNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return "Nombre inválido";
        String limpio = nombre.trim();
        return limpio.substring(0, 1).toUpperCase() + limpio.substring(1).toLowerCase();
    }

    public static boolean esPalindromo(String texto) {
    if (texto == null) return false;
    String limpio = texto.replaceAll("\\s+", "").toLowerCase();
    return limpio.equals(new StringBuilder(limpio).reverse().toString());
}

public static int maximo(int a, int b) {
    return Math.max(a, b);
}

public static String clasificarTemperatura(int temp) {
    if (temp < 0) {
        return "Congelante";
    } else if (temp < 15) {
        return "Frío";
    } else if (temp < 30) {
        return "Templado";
    } else {
        return "Caliente";
    }
}

public static boolean contieneNumero(String texto) {
    if (texto == null) return false;
    for (int i = 0; i < texto.length(); i++) {
        if (Character.isDigit(texto.charAt(i))) {
            return true;
        }
    }
    return false;
}

public static String calificarNota(double nota) {
    if (nota < 0 || nota > 5) return "Inválida";
    if (nota >= 4.5) return "Excelente";
    if (nota >= 3.5) return "Buena";
    if (nota >= 2.5) return "Regular";
    return "Insuficiente";
}

}
