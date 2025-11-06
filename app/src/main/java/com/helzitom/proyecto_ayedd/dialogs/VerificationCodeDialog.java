package com.helzitom.proyecto_ayedd.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.helzitom.proyecto_ayedd.R;

/**
 * Clase que muestra un cuadro de diálogo personalizado para verificar un código de autenticación.
 * Permite al usuario ingresar un código de 6 dígitos, verificarlo y manejar intentos fallidos.
 */
public class VerificationCodeDialog extends Dialog {

    // Campos de la interfaz
    private EditText etCode;
    private Button btnVerify, btnCancel;
    private TextView tvResend;

    // Código correcto que se debe validar
    private String correctCode;

    // Listener para manejar el resultado de la verificación
    private OnVerificationListener listener;

    // Número máximo de intentos permitidos
    private int attemptsLeft = 3;

    /**
     * Interfaz que define los métodos de retorno cuando la verificación es exitosa o falla.
     */
    public interface OnVerificationListener {
        void onVerificationSuccess();
        void onVerificationFailed();
    }

    /**
     * Constructor del diálogo.
     *
     * @param context Contexto actual.
     * @param verificationCode Código correcto que se debe verificar.
     * @param onSuccess Acción a ejecutar si la verificación es exitosa.
     * @param onFailed Acción a ejecutar si la verificación falla o se cancela.
     */
    public VerificationCodeDialog(Context context, String verificationCode,
                                  Runnable onSuccess, Runnable onFailed) {
        super(context);
        this.correctCode = verificationCode;

        // Se crea una implementación anónima del listener
        this.listener = new OnVerificationListener() {
            @Override
            public void onVerificationSuccess() {
                onSuccess.run();
            }

            @Override
            public void onVerificationFailed() {
                onFailed.run();
            }
        };
    }

    /**
     * Método que se ejecuta al crear el diálogo.
     * Inicializa la vista, configura los eventos y hace transparente el fondo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_verification);

        // Hacer el fondo del diálogo transparente
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Inicializar vistas y configurar eventos
        initViews();
        setupListeners();

        // Impedir que se cierre tocando fuera del cuadro
        setCancelable(false);
    }


    //Inicializa las vistas del layout (EditText, botones y texto).

    private void initViews() {
        etCode = findViewById(R.id.etVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnCancel = findViewById(R.id.btnCancel);
        tvResend = findViewById(R.id.tvResendCode);
    }

    /**
     * Configura los eventos para los botones y el campo de texto.
     * Verifica el código cuando se presiona "Verificar".
     * Cancela el diálogo con "Cancelar".
     * Permite reenviar el código y reinicia los intentos.
     * Habilita el botón "Verificar" solo cuando hay 6 dígitos.
     */
    private void setupListeners() {
        // Botón para verificar el código ingresado
        btnVerify.setOnClickListener(v -> verifyCode());

        // Botón para cancelar y cerrar el diálogo
        btnCancel.setOnClickListener(v -> {
            listener.onVerificationFailed();
            dismiss();
        });

        // Texto para reenviar el código
        tvResend.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
            attemptsLeft = 3; // Reinicia los intentos
        });

        // Verifica si el usuario ha ingresado los 6 dígitos
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Habilitar el botón solo si hay 6 dígitos
                btnVerify.setEnabled(s.length() == 6);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Verifica si el código ingresado por el usuario coincide con el código correcto.
     * Si el código es correcto, se llama a onVerificationSuccess().
     * Si es incorrecto, se reduce el número de intentos y se muestra un mensaje de error.
     * Si se acaban los intentos, se ejecuta onVerificationFailed().
     */
    private void verifyCode() {
        String enteredCode = etCode.getText().toString().trim();

        if (enteredCode.equals(correctCode)) {
            Toast.makeText(getContext(), "¡Código verificado!", Toast.LENGTH_SHORT).show();
            listener.onVerificationSuccess();
            dismiss();
        } else {
            attemptsLeft--;

            if (attemptsLeft > 0) {
                etCode.setError("Código incorrecto. Te quedan " + attemptsLeft + " intentos");
                etCode.setText("");
                etCode.requestFocus();
            } else {
                Toast.makeText(getContext(), "Intentos agotados", Toast.LENGTH_SHORT).show();
                listener.onVerificationFailed();
                dismiss();
            }
        }
    }
}
