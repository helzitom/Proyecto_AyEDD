package com.helzitom.proyecto_ayedd.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.helzitom.proyecto_ayedd.fragments.PedidosFragment;
import com.helzitom.proyecto_ayedd.fragments.EmployeesFragment;

public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PedidosFragment();
            case 1:
                return new EmployeesFragment();
            default:
                return new PedidosFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 pestañas
    }
}