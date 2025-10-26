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
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.EmployeeService;

import java.io.Serializable;

public class EditEmployeeDialog extends DialogFragment {

    private static final String ARG_EMPLOYEE = "employee";
    private TextInputEditText etName, etLastname, etUsername, etEmail;
    private RadioGroup rgType;
    private Button btnCancel, btnSave;
    private EmployeeService employeeService;
    private User employee;
    private OnEmployeeUpdatedListener listener;

    public interface OnEmployeeUpdatedListener {
        void loadEmployees();
    }

    public static EditEmployeeDialog newInstance(User employee) {
        EditEmployeeDialog dialog = new EditEmployeeDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EMPLOYEE, (Serializable) employee);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnEmployeeUpdatedListener(OnEmployeeUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            employee = (User) getArguments().getSerializable(ARG_EMPLOYEE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_employee, null);

        initViews(view);
        fillEmployeeData();
        setupListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_employee_name);
        etLastname = view.findViewById(R.id.et_employee_lastname);
        etUsername = view.findViewById(R.id.et_employee_username);
        etEmail = view.findViewById(R.id.et_employee_email);
        rgType = view.findViewById(R.id.rg_employee_type);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);

        // Ocultar campo de contraseña en edición
        View passwordLayout = view.findViewById(R.id.et_employee_password).getRootView()
                .findViewById(R.id.et_employee_password);
        if (passwordLayout != null && passwordLayout.getParent() != null) {
            ((View) passwordLayout.getParent()).setVisibility(View.GONE);
        }

        // Deshabilitar email (no se puede cambiar)
        etEmail.setEnabled(false);

        employeeService = new EmployeeService();
    }

    private void fillEmployeeData() {
        if (employee != null) {
            etName.setText(employee.getName());
            etLastname.setText(employee.getLastname());
            etUsername.setText(employee.getUsername());
            etEmail.setText(employee.getEmail());

            // Seleccionar tipo
            if ("delivery".equals(employee.getType())) {
                rgType.check(R.id.rb_delivery);
            } else if ("receiver".equals(employee.getType())) {
                rgType.check(R.id.rb_receiver);
            }
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> updateEmployee());
    }

    private void updateEmployee() {
        String name = etName.getText().toString().trim();
        String lastname = etLastname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (name.isEmpty() || lastname.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgType.getCheckedRadioButtonId();
        String type;
        if (selectedId == R.id.rb_delivery) {
            type = "delivery";
        } else if (selectedId == R.id.rb_receiver) {
            type = "receiver";
        } else {
            Toast.makeText(requireContext(), "Selecciona un tipo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Actualizando...");

        employeeService.actualizarEmpleado(employee.getUserId(), name, lastname, username, type,
                new EmployeeService.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Empleado actualizado", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.loadEmployees();
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