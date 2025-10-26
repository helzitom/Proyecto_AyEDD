package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.dialogs.VerificationCodeDialog;
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.EmailService;
import com.helzitom.proyecto_ayedd.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etLastname, etUsername, etEmail, etPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthService authService;
    private EmailService emailService;

    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFirebase();
        initializeViews();
        setupListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authService = new AuthService(mAuth, db);
        emailService = new EmailService();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etLastname = findViewById(R.id.etLastname);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.button2);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        // Obtener datos
        String name = etName.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String type = "customer";

        // Validar campos vacíos
        if (!validateEmptyFields(name, lastname, username, email, password)) {
            return;
        }

        // Validar formatos
        if (!validateFormats(name, lastname, username, email, password)) {
            return;
        }

        // Verificar disponibilidad de username y email
        checkAvailabilityAndProceed(name, lastname, username, email, type, password);
    }

    private boolean validateEmptyFields(String name, String lastname, String username,
                                        String email, String password) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es requerido");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastname)) {
            etLastname.setError("El apellido es requerido");
            etLastname.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("El usuario es requerido");
            etUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("El email es requerido");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es requerida");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateFormats(String name, String lastname, String username,
                                    String email, String password) {
        // Validar nombre y apellido (solo letras)
        if (!ValidationUtils.isValidName(name)) {
            etName.setError("El nombre solo debe contener letras");
            etName.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidName(lastname)) {
            etLastname.setError("El apellido solo debe contener letras");
            etLastname.requestFocus();
            return false;
        }

        // Validar username (alfanumérico, min 4 caracteres)
        if (!ValidationUtils.isValidUsername(username)) {
            etUsername.setError("Usuario inválido (mín. 4 caracteres, solo letras y números)");
            etUsername.requestFocus();
            return false;
        }

        // Validar email
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return false;
        }

        // Validar contraseña (mínimo 6 caracteres)
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void checkAvailabilityAndProceed(String name, String lastname, String username,
                                             String email, String type, String password) {
        btnRegister.setEnabled(false);

        authService.checkUsernameAvailability(username, isAvailable -> {
            if (!isAvailable) {
                etUsername.setError("Este usuario ya está en uso");
                etUsername.requestFocus();
                btnRegister.setEnabled(true);
                return;
            }

            // Si el username está disponible, generar código y enviar email
            sendVerificationEmail(name, lastname, username, email, type, password);
        });
    }

    private void sendVerificationEmail(String name, String lastname, String username,
                                       String email, String type, String password) {
        // Generar código de 6 dígitos
        verificationCode = ValidationUtils.generateVerificationCode();

        // Enviar email con código
        emailService.sendVerificationEmail(email, name, verificationCode, success -> {
            if (success) {
                // Mostrar diálogo para ingresar código
                showVerificationDialog(name, lastname, username, email, type, password);
            } else {
                Toast.makeText(this, "Error al enviar el código de verificación",
                        Toast.LENGTH_SHORT).show();
                btnRegister.setEnabled(true);
            }
        });
    }

    private void showVerificationDialog(String name, String lastname, String username,
                                        String email, String type, String password) {
        VerificationCodeDialog dialog = new VerificationCodeDialog(
                this,
                verificationCode,
                () -> {
                    // Código verificado correctamente, proceder con el registro
                    registerUserInFirebase(name, lastname, username, email, type, password);
                },
                () -> {
                    // Usuario canceló o código incorrecto
                    btnRegister.setEnabled(true);
                }
        );
        dialog.show();
    }

    private void registerUserInFirebase(String name, String lastname, String username,
                                        String email, String type, String password) {
        User user = new User(name, lastname, username, email,type);

        authService.registerUser(user, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                Toast.makeText(RegisterActivity.this,
                        "Registro exitoso", Toast.LENGTH_SHORT).show();

                // Navegar a la actividad de usuario
                Intent intent = new Intent(RegisterActivity.this, CustomerActivity.class);
                intent.putExtra("userId", userId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RegisterActivity.this,
                        "Error en el registro: " + error, Toast.LENGTH_LONG).show();
                btnRegister.setEnabled(true);
            }
        });
    }
}