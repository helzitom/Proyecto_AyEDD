package com.helzitom.proyecto_ayedd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.dialogs.EditEmployeeDialog;
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.EmployeeService;

import java.util.List;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder> {

    private List<User> employees;
    private Context context;
    private OnEmployeeChangedListener listener;

    public interface OnEmployeeChangedListener {
        void loadEmployees();
    }

    public EmployeesAdapter(List<User> employees, Context context, OnEmployeeChangedListener listener) {
        this.employees = employees;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        User employee = employees.get(position);
        holder.bind(employee);
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public void updateList(List<User> newEmployees) {
        this.employees = newEmployees;
        notifyDataSetChanged();
    }

    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail, tvUsername;
        Chip chipType;
        Button btnEdit, btnDelete;
        EmployeeService employeeService;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_employee_name);
            tvEmail = itemView.findViewById(R.id.tv_employee_email);
            tvUsername = itemView.findViewById(R.id.tv_employee_username);
            chipType = itemView.findViewById(R.id.chip_employee_type);
            btnEdit = itemView.findViewById(R.id.btn_edit_employee);
            btnDelete = itemView.findViewById(R.id.btn_delete_employee);
            employeeService = new EmployeeService();
        }

        void bind(User employee) {
            // Avatar con inicial
            String name = employee.getName() != null ? employee.getName() : "?";
            tvAvatar.setText(name.substring(0, 1).toUpperCase());

            // Información
            String fullName = (employee.getName() != null ? employee.getName() : "") +
                    " " + (employee.getLastname() != null ? employee.getLastname() : "");
            tvName.setText(fullName.trim());
            tvEmail.setText(employee.getEmail());
            tvUsername.setText("@" + (employee.getUsername() != null ? employee.getUsername() : "sin_usuario"));

            // Tipo
            String type = employee.getType();
            if ("delivery".equals(type)) {
                chipType.setText("🚚 Delivery");
                chipType.setChipBackgroundColorResource(android.R.color.holo_blue_light);
            } else if ("receiver".equals(type)) {
                chipType.setText("📥 Recepción");
                chipType.setChipBackgroundColorResource(android.R.color.holo_green_light);
            }

            // Botones
            btnEdit.setOnClickListener(v -> {
                EditEmployeeDialog dialog = EditEmployeeDialog.newInstance(employee);
                dialog.setOnEmployeeUpdatedListener(() -> {
                    if (listener != null) {
                        listener.loadEmployees();
                    }
                });
                dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "EditEmployee");
            });

            btnDelete.setOnClickListener(v -> showDeleteConfirmation(employee));
        }

        private void showDeleteConfirmation(User employee) {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Empleado")
                    .setMessage("¿Estás seguro de eliminar a " + employee.getName() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> deleteEmployee(employee))
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private void deleteEmployee(User employee) {
            employeeService.eliminarEmpleado(employee.getUserId(), new EmployeeService.DeleteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Empleado eliminado", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.loadEmployees();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}