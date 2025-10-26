package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.FirebaseManager;
import com.helzitom.proyecto_ayedd.services.UserService;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 segundos
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ocultar Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash);

        // Inicializar Firebase
        FirebaseManager.getInstance().initialize(this);
        authService = new AuthService();

        // Esperar 3 segundos y verificar sesión
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                verificarSesion();
            }
        }, SPLASH_DURATION);
    }

    private void verificarSesion() {
        if (authService.isUserLoggedIn()) {
            // Hay sesión activa, obtener tipo y redirigir
            authService.obtenerTipoUsuarioActual(new UserService.TipoUsuarioCallback() {
                @Override
                public void onSuccess(String tipo) {
                    redirigirSegunTipo(tipo);
                }

                @Override
                public void onError(String error) {
                    // Error al obtener tipo, cerrar sesión e ir a login
                    authService.logout();
                    irALogin();
                }
            });
        } else {
            // No hay sesión, ir a login (MainActivity)
            irALogin();
        }
    }

    private void redirigirSegunTipo(String tipo) {
        Intent intent;

        switch (tipo) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "delivery":
                intent = new Intent(this, DeliveryActivity.class);
                break;
            case "receiver":
                intent = new Intent(this, ReceiverActivity.class);
                break;
            case "customer":
            default:
                intent = new Intent(this, CustomerActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void irALogin() {
        // MainActivity es el Login
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
