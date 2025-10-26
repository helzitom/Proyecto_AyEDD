package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.AdminPagerAdapter;
import com.helzitom.proyecto_ayedd.dialogs.AddEmployeeDialog;
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.EmployeeService;
import com.helzitom.proyecto_ayedd.services.FirebaseManager;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fab;
    private AdminPagerAdapter pagerAdapter;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        FirebaseManager.getInstance().initialize(this);
        authService = new AuthService(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());

        initViews();
        setupToolbar();
        setupDrawer();
        setupViewPager();
        setupFab();

    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fab = findViewById(R.id.fab_add);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewPager() {
        pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pedidos");
                    break;
                case 1:
                    tab.setText("Empleados");
                    break;
            }
        }).attach();

        // Cambiar icono del FAB según la pestaña
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateFabIcon(position);
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            int currentTab = viewPager.getCurrentItem();
            if (currentTab == 1) { // Pestaña de empleados
                showAddEmployeeDialog();
            } else {
                Toast.makeText(this, "Funcionalidad de agregar pedido próximamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFabIcon(int position) {
        if (position == 1) { // Empleados
            fab.setImageResource(android.R.drawable.ic_input_add);
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void showAddEmployeeDialog() {
        AddEmployeeDialog dialog = new AddEmployeeDialog();
        dialog.show(getSupportFragmentManager(), "AddEmployeeDialog");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_pedidos) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.nav_employees) {
            viewPager.setCurrentItem(1);
        } else  if (id == R.id.nav_perfil) {
            Toast.makeText(this, "Mi perfil - Próximamente", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void logout() {
        authService.logout();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}