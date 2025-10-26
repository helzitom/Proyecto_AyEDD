package com.helzitom.proyecto_ayedd.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.EmployeesAdapter;
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.EmployeeService;

import java.util.ArrayList;
import java.util.List;

public class EmployeesFragment extends Fragment {
    private static final String TAG = "EmployeesFragment";

    private RecyclerView rvEmployees;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private Chip chipAllEmployees, chipDelivery, chipReceiver;

    private EmployeesAdapter adapter;
    private EmployeeService employeeService;
    private List<User> allEmployees;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_fragment_employees_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        loadEmployees();
    }

    private void initViews(View view) {
        rvEmployees = view.findViewById(R.id.rv_employees);
        layoutEmpty = view.findViewById(R.id.layout_empty_employees);
        progressBar = view.findViewById(R.id.progress_bar_employees);
        chipAllEmployees = view.findViewById(R.id.chip_all_employees);
        chipDelivery = view.findViewById(R.id.chip_delivery);
        chipReceiver = view.findViewById(R.id.chip_receiver);

        employeeService = new EmployeeService();
        allEmployees = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new EmployeesAdapter(new ArrayList<>(), requireContext(), this::refreshEmployees);
        rvEmployees.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEmployees.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAllEmployees.setOnClickListener(v -> filterEmployees("all"));
        chipDelivery.setOnClickListener(v -> filterEmployees("delivery"));
        chipReceiver.setOnClickListener(v -> filterEmployees("receiver"));
    }

    public void loadEmployees() {
        showLoading(true);

        employeeService.obtenerTodosEmpleados(new EmployeeService.EmployeesListCallback() {
            @Override
            public void onSuccess(List<User> employees) {
                Log.d(TAG, "✅ Empleados cargados: " + employees.size());
                allEmployees = employees;
                filterEmployees(currentFilter);
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
                showEmpty(true);
                showLoading(false);
            }
        });
    }

    private void filterEmployees(String filter) {
        currentFilter = filter;
        List<User> filteredList = new ArrayList<>();

        for (User employee : allEmployees) {
            switch (filter) {
                case "all":
                    filteredList.add(employee);
                    break;
                case "delivery":
                    if ("delivery".equals(employee.getType())) {
                        filteredList.add(employee);
                    }
                    break;
                case "receiver":
                    if ("receiver".equals(employee.getType())) {
                        filteredList.add(employee);
                    }
                    break;
            }
        }

        adapter.updateList(filteredList);
        showEmpty(filteredList.isEmpty());
    }

    private void refreshEmployees() {
        loadEmployees();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvEmployees.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvEmployees.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}