package com.helzitom.proyecto_ayedd.utils;

import android.util.Patterns;
import java.util.Random;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Validar nombre (solo letras y espacios)
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
        return pattern.matcher(name).matches() && name.length() >= 2;
    }

    // Validar username (alfanumérico, sin espacios, min 4 caracteres)
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
        return pattern.matcher(username).matches();
    }

    // Validar email
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validar contraseña (mínimo 6 caracteres)
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    // Validar contraseña fuerte (opcional)
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Al menos una mayúscula, una minúscula, un número
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
        return pattern.matcher(password).matches();
    }

    // Generar código de verificación de 6 dígitos
    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}