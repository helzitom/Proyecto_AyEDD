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
public class VerificationCodeDialog extends Dialog {

    private EditText etCode;
    private Button btnVerify, btnCancel;
    private TextView tvResend;

    private String correctCode;
    private OnVerificationListener listener;
    private int attemptsLeft = 3;

    public interface OnVerificationListener {
        void onVerificationSuccess();
        void onVerificationFailed();
    }

    public VerificationCodeDialog(Context context, String verificationCode,
                                  Runnable onSuccess, Runnable onFailed) {
        super(context);
        this.correctCode = verificationCode;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_verification);

        // Hacer el fondo transparente
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupListeners();

        setCancelable(false); // No se puede cerrar tocando fuera
    }

    private void initViews() {
        etCode = findViewById(R.id.etVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnCancel = findViewById(R.id.btnCancel);
        tvResend = findViewById(R.id.tvResendCode);
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> verifyCode());

        btnCancel.setOnClickListener(v -> {
            listener.onVerificationFailed();
            dismiss();
        });

        tvResend.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Código reenviado", Toast.LENGTH_SHORT).show();
            attemptsLeft = 3; // Resetear intentos
        });

        // Auto verificar cuando tenga 6 dígitos
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    btnVerify.setEnabled(true);
                } else {
                    btnVerify.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

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