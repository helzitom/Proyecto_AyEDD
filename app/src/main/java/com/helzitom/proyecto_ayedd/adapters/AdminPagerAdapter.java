
package com.helzitom.proyecto_ayedd.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.helzitom.proyecto_ayedd.fragments.EmployeesFragment;
import com.helzitom.proyecto_ayedd.fragments.PedidosFragment;

public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Devuelve el fragmento correspondiente a la pestaña
        switch (position) {
            case 0:
                return new PedidosFragment();
            case 1:
                return new EmployeesFragment();
            default:
                // En caso de posición no válida, retorna el primero
                return new PedidosFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Número total de pestañas
    }
}
