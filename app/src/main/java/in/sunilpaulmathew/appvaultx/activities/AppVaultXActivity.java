package in.sunilpaulmathew.appvaultx.activities;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AccessUnavilableDialog;
import in.sunilpaulmathew.appvaultx.dialogs.WelcomeDialog;
import in.sunilpaulmathew.appvaultx.fragments.InstalledPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.RemovedPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.SettingsFragment;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuPermissionChecker;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class AppVaultXActivity extends AppCompatActivity {

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize App Theme
        Settings.initializeAppTheme(this);

        setContentView(R.layout.activity_main);

        updateInstallerActivityState();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        View layoutRoot = findViewById(R.id.layout_root);
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);

        ViewCompat.setOnApplyWindowInsetsListener(layoutRoot, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    0
            );

            return insets;
        });

        Menu menu = bottomNav.getMenu();
        menu.add(Menu.NONE, 0, Menu.NONE, null).setIcon(R.drawable.ic_apps);
        menu.add(Menu.NONE, 1, Menu.NONE, null).setIcon(R.drawable.ic_delete);
        menu.add(Menu.NONE, 2, Menu.NONE, null).setIcon(R.drawable.ic_menu);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == 0) fragment = new InstalledPackagesFragment();
            else if (item.getItemId() == 1) fragment = new RemovedPackagesFragment();
            else if (item.getItemId() == 2) fragment = new SettingsFragment();

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commitAllowingStateLoss();
            }
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(0);
        }

        bottomNav.post(() -> fragmentContainer.setPadding(0, 0, 0, bottomNav.getHeight()));

        if (!Utils.getBoolean("welcome_dialog_viewed", false, this)) {
            new WelcomeDialog(this);
        }

        if (!Settings.isShizukuIgnored(this)) {
            if (Shizuku.pingBinder() && Shizuku.getVersion() >= 11) {
                new ShizukuPermissionChecker(this) {
                    @Override
                    public void onRequestingPermission() {
                        // Request permission
                        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
                        Shizuku.requestPermission(0);
                    }

                    @Override
                    public void onFinished() {
                    }
                };
            } else {
                new AccessUnavilableDialog(this).show();
            }
        }
    }

    private void updateInstallerActivityState() {
        boolean shizukuAvailable = Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        boolean shouldEnable = !Utils.isGooglePlayVersion(this) && shizukuAvailable && !Settings.isShizukuIgnored(this);

        ComponentName installerActivity = new ComponentName(this, InstallerActivity.class);
        PackageManager pm = getPackageManager();

        int currentState = pm.getComponentEnabledSetting(installerActivity);
        int desiredState = shouldEnable ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (currentState != desiredState) {
            pm.setComponentEnabledSetting(
                    installerActivity,
                    desiredState,
                    PackageManager.DONT_KILL_APP
            );
        }
    }

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        if (requestCode == 0 && grantResult == PackageManager.PERMISSION_GRANTED) {
            Utils.toast("AppVaultX got access to Shizuku service", this).show();
            ShizukuShell.ensureUserService(null);
        } else {
            Utils.toast(getString(R.string.shizuku_access_denied_title), this).show();
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