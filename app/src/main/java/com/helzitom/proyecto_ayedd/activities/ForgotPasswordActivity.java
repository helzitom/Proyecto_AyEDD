package com.helzitom.proyecto_ayedd.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.helzitom.proyecto_ayedd.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private EditText etEmail;
    private Button btnEnviarEmail;
    private ProgressBar progressBar;
    private TextView tvVolverLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_forgot_email);
        btnEnviarEmail = findViewById(R.id.btn_enviar_email);
        progressBar = findViewById(R.id.progress_bar_forgot);
        tvVolverLogin = findViewById(R.id.tv_volver_login);

        progressBar.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnEnviarEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarEmailRecuperacion();
            }
        });

        tvVolverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void enviarEmailRecuperacion() {
        String email = etEmail.getText().toString().trim();

        // Validar email
        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo electrónico");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }

        // Mostrar progreso
        btnEnviarEmail.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Enviar email de recuperación
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        btnEnviarEmail.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "✅ Se ha enviado un correo de recuperación a " + email,
                                    Toast.LENGTH_LONG).show();
                            finish(); // Volver al login
                        } else {
                            String errorMsg = "Error al enviar el correo";
                            if (task.getException() != null) {
                                errorMsg = obtenerMensajeError(task.getException().getMessage());
                            }
                            Toast.makeText(ForgotPasswordActivity.this,
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private String obtenerMensajeError(String error) {
        if (error == null) return "Error desconocido";

        if (error.contains("no user record")) {
            return "No existe una cuenta con este correo electrónico";
        } else if (error.contains("network")) {
            return "Error de conexión. Verifica tu internet";
        } else if (error.contains("invalid-email")) {
            return "Correo electrónico inválido";
        } else {
            return "Error al enviar correo de recuperación";
        }
    }
}