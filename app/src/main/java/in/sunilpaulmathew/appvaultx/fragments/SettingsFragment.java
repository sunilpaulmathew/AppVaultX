package in.sunilpaulmathew.appvaultx.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.appvaultx.BuildConfig;
import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.SettingsAdapter;
import in.sunilpaulmathew.appvaultx.serializable.SettingsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class SettingsFragment extends Fragment {

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayoutCompat titleLayout = mRootView.findViewById(R.id.title_layout);
        MaterialTextView title = mRootView.findViewById(R.id.title);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);

        String titleText = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;
        title.setText(titleText);

        recyclerView.setItemAnimator(null);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        recyclerView.setAdapter(new SettingsAdapter(getData(), REQUEST_PERMISSION_RESULT_LISTENER));

        titleLayout.setOnClickListener(v -> {
            Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            settings.setData(uri);
            startActivity(settings);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Settings.navigateToFragment(0, requireActivity());
            }
        });

        return mRootView;
    }

    private List<SettingsEntry> getData() {
        List<SettingsEntry> mData = new ArrayList<>();
        mData.add(new SettingsEntry(getString(R.string.user_interface)));
        mData.add(new SettingsEntry(1, R.drawable.ic_theme, getString(R.string.app_theme), Settings.getAppTheme(requireActivity())));
        mData.add(new SettingsEntry(getString(R.string.general)));
        mData.add(new SettingsEntry(2, R.drawable.ic_power, getString(R.string.shizuku_mode), Settings.getShizukuMode(requireActivity())));
        mData.add(new SettingsEntry(3, R.drawable.ic_warning, getString(R.string.unsafe_removal_prevention), getString(R.string.unsafe_removal_prevention_description), true, Utils.getBoolean("unsafeAppRemovalProtection", true, requireActivity())));
        mData.add(new SettingsEntry(getString(R.string.miscellaneous)));
        mData.add(new SettingsEntry(4, R.drawable.ic_github, getString(R.string.source_code), getString(R.string.source_code_description)));
        mData.add(new SettingsEntry(5, R.drawable.ic_learn, getString(R.string.shizuku_learn), getString(R.string.shizuku_learn_description)));
        mData.add(new SettingsEntry(6, R.drawable.ic_learn, getString(R.string.uad_learn), getString(R.string.uad_learn_description)));
        mData.add(new SettingsEntry(7, R.drawable.ic_translate, getString(R.string.translations), getString(R.string.translations_description)));
        mData.add(new SettingsEntry(8, R.drawable.ic_privacy, getString(R.string.privacy_policy), getString(R.string.privacy_policy_description)));
        mData.add(new SettingsEntry(9, R.drawable.ic_email, getString(R.string.developer_contact), getString(R.string.developer_contact_description)));
        return mData;
    }

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        if (requestCode == 0 && grantResult == PackageManager.PERMISSION_GRANTED) {
            Utils.toast("AppVaultX got access to Shizuku service", requireActivity()).show();
            ShizukuShell.ensureUserService(null);
            requireActivity().recreate();
        } else {
            Utils.toast(getString(R.string.shizuku_access_denied_title), requireActivity()).show();
        }
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        ShizukuShell.destroy();
    }

}