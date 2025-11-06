package com.helzitom.proyecto_ayedd.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.services.EmployeeService;

//Clase el cuadro de diálogo al crear nuevo empleado
public class AddEmployeeDialog extends DialogFragment {

    private TextInputEditText etName, etLastname, etUsername, etEmail, etPassword;
    private RadioGroup rgType;
    private Button btnCancel, btnSave;
    private EmployeeService employeeService;

    // 🔹 Interfaz para notificar que se añadió un empleado
    public interface OnEmployeeAddedListener {
        void onEmployeeAdded();
    }

    private OnEmployeeAddedListener listener;

    // 🔹 Setter para asignar el listener desde AdminActivity
    public void setOnEmployeeAddedListener(OnEmployeeAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_employee, null);

        initViews(view);
        setupListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_employee_name);
        etLastname = view.findViewById(R.id.et_employee_lastname);
        etUsername = view.findViewById(R.id.et_employee_username);
        etEmail = view.findViewById(R.id.et_employee_email);
        etPassword = view.findViewById(R.id.et_employee_password);
        rgType = view.findViewById(R.id.rg_employee_type);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);

        employeeService = new EmployeeService();
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveEmployee());
    }

    private void saveEmployee() {
        String name = etName.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || lastname.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgType.getCheckedRadioButtonId();
        String type;
        if (selectedId == R.id.rb_delivery) {
            type = "delivery";
        } else if (selectedId == R.id.rb_receiver) {
            type = "receiver";
        } else {
            Toast.makeText(requireContext(), "Selecciona un tipo de empleado", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Creando...");

        employeeService.crearEmpleado(email, password, name, lastname, username, type,
                new EmployeeService.CreateEmployeeCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Empleado creado exitosamente", Toast.LENGTH_SHORT).show();

                        // 🔥 Notificar al AdminActivity
                        if (listener != null) {
                            listener.onEmployeeAdded();
                        }

                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Guardar");
                    }
                });
    }
}
