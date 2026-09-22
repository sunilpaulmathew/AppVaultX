package in.sunilpaulmathew.appvaultx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.PagerAdapter;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import in.sunilpaulmathew.appvaultx.viewModels.ApplicationsViewModel;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class InstalledPackagesFragment extends Fragment {

    protected ApplicationsViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packages_installed, container, false);

        MaterialButton searchButton = mRootView.findViewById(R.id.search);
        MaterialButton sortButton = mRootView.findViewById(R.id.sort);
        TabLayout mTabLayout = mRootView.findViewById(R.id.tab_layout);
        ViewPager2 mViewPager = mRootView.findViewById(R.id.view_pager);

        mViewModel = new ViewModelProvider(requireActivity()).get(ApplicationsViewModel.class);

        PagerAdapter adapter = new PagerAdapter(requireActivity());
        for (int i=0; i<getLabels().length; i++) {
            adapter.addFragment(ApplicationsFragment.newInstance(i), getLabels()[i]);
        }
        mViewPager.setAdapter(adapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        mViewPager.setCurrentItem(Utils.getInt("appType", 0, requireActivity()), false);

        searchButton.setOnClickListener(v -> mViewModel.triggerSearch());

        sortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, Menu.NONE, R.string.reverse).setIcon(R.drawable.ic_sort_az).setCheckable(true).setChecked(Utils.getBoolean("reverse_order", false, requireActivity()));

            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    mViewModel.triggerSort();
                }
                return false;
            });
            popupMenu.show();
        });

        return mRootView;
    }

    private String[] getLabels() {
        return new  String[] {
                getString(R.string.app_type_system_title),
                getString(R.string.app_type_system_updated_title),
                getString(R.string.app_type_user_title),
                getString(R.string.app_type_debuggable_title),
                getString(R.string.uad_status_recommended_title),
                getString(R.string.uad_status_advanced_title),
                getString(R.string.uad_status_expert_title),
                getString(R.string.uad_status_unsafe_title)
        };
    }

}