package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvOlvidarContrasena;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseManager.getInstance().initialize(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        tvOlvidarContrasena = findViewById(R.id.tv_forgot_password);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void goToForgotPassword(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingrese su correo");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingrese su contraseña");
            return;
        }

        // Mostrar barra y desactivar botón
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Ocultar barra y reactivar botón
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserTypeAndRedirect(firebaseUser.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserTypeAndRedirect(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            redirectToActivity(user.getType());
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void redirectToActivity(String userType) {
        Intent intent;
        switch (userType.toLowerCase()) {
            case "delivery":
                intent = new Intent(this, DeliveryActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "receiver":
                intent = new Intent(this, ReceiverActivity.class);
                break;
            case "customer":
                intent = new Intent(this, CustomerActivity.class);
                break;
            default:
                Toast.makeText(this, "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }
}