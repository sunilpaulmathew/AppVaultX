package in.sunilpaulmathew.appvaultx.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AccessUnavilableDialog;
import in.sunilpaulmathew.appvaultx.dialogs.WelcomeDialog;
import in.sunilpaulmathew.appvaultx.fragments.InstalledPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.RemovedPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.SettingsFragment;
import in.sunilpaulmathew.appvaultx.utils.CrashReport;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuPermissionChecker;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import navView.NavView;
import navView.serializables.NavViewEntry;
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

        // Initialize Crash Reporter
        new CrashReport(this).initialize();

        NavView mNavView = findViewById(R.id.nav_view);
        View layoutRoot = findViewById(R.id.layout_root);

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

        List<NavViewEntry> data = new CopyOnWriteArrayList<>();
        data.add(new NavViewEntry(InstalledPackagesFragment::new, R.drawable.ic_apps));
        data.add(new NavViewEntry(RemovedPackagesFragment::new, R.drawable.ic_delete));
        data.add(new NavViewEntry(SettingsFragment::new, R.drawable.ic_menu));

        mNavView.setNavigationItems(data);

        mNavView.setOnItemSelectedListener((fragment, position) -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new InstalledPackagesFragment()).commit();
        }

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