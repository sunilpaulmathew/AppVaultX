package in.sunilpaulmathew.appvaultx.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AccessUnavilableDialog;
import in.sunilpaulmathew.appvaultx.dialogs.WelcomeDialog;
import in.sunilpaulmathew.appvaultx.fragments.InstalledPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.RemovedPackagesFragment;
import in.sunilpaulmathew.appvaultx.fragments.SettingsFragment;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.CrashReport;
import in.sunilpaulmathew.appvaultx.utils.Packages;
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
public class AppVaultXActivity extends BaseActivity {

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize App Theme
        Settings.initializeAppTheme(this);

        setContentView(R.layout.activity_main, R.id.layout_root);

        ContentLoadingProgressBar progressBar = findViewById(R.id.progress);
        NavView mNavView = findViewById(R.id.nav_view);
        LinearLayoutCompat progressLayout = findViewById(R.id.progress_layout);

        // Initialize Crash Reporter
        new CrashReport(this).initialize();

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

        new Async() {
            private final Activity activity = AppVaultXActivity.this;
            public void onPreExecute() {
                progressLayout.setVisibility(VISIBLE);
            }

            @Override
            public void doInBackground() {
                // Load package details on first app launch
                if (!Packages.isDataLoaded()) {
                    Packages.loadPackages(progressBar, activity);
                }
            }

            private List<NavViewEntry> getData() {
                List<NavViewEntry> data = new CopyOnWriteArrayList<>();
                data.add(new NavViewEntry(InstalledPackagesFragment::new, R.drawable.ic_apps));
                data.add(new NavViewEntry(RemovedPackagesFragment::new, R.drawable.ic_delete));
                data.add(new NavViewEntry(SettingsFragment::new, R.drawable.ic_menu));
                return data;
            }

            @Override
            public void onPostExecute() {
                if ( activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }

                mNavView.setNavigationItems(getData());

                mNavView.setOnItemSelectedListener((fragment, position) -> getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                );

                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new InstalledPackagesFragment()).commit();
                }

                progressLayout.setVisibility(GONE);
            }
        }.execute();
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