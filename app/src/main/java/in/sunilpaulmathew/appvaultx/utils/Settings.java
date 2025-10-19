package in.sunilpaulmathew.appvaultx.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PolicyEntry;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class Settings {

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isShizukuIgnored(Context context) {
        return Utils.getInt("shizuku_mode", 0, context) == 1;
    }

    public static int getColorAccent(Context context) {
        return getMaterial3Colors(Utils.getColor(R.color.colorBlue, context), context);
    }

    public static int getColorText(Context context) {
        return Utils.getColor(isDarkTheme(context) ? R.color.colorWhite : R.color.colorBlack, context);
    }

    private static int getMaterial3Colors(int defaultColor, Context context) {
        int color = defaultColor;
        if (DynamicColors.isDynamicColorAvailable()) {
            Context dynamicClrCtx = DynamicColors.wrapContextIfAvailable(context, com.google.android.material.R.style.MaterialAlertDialog_Material3);
            TypedArray ta = dynamicClrCtx.obtainStyledAttributes(new int[] {
                    androidx.appcompat.R.attr.colorPrimary
            });
            color = ta.getColor(0, defaultColor);
            ta.recycle();
        }
        return color;
    }

    public static int getAppThemePosition(Context context) {
        return Utils.getInt("appTheme", 0, context);
    }

    public static int getShizukuModePosition(Context context) {
        if (isShizukuIgnored(context)) {
            return 1;
        }
        if (Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            return 0;
        } else {
            return 1;
        }
    }

    public static List<PolicyEntry> getPolicyData() {
        List<PolicyEntry> mData = new ArrayList<>();
        mData.add(new PolicyEntry("Introduction", "AppVaultX is developed by one main developer, sunilpaulmathew, leveraging code from various open-source projects. This Privacy Policy outlines how we handle user privacy."));
        mData.add(new PolicyEntry("Scope", "This policy applies exclusively to the original version of AppVaultX published by the developer on IzzyOnDroid and GitHub."));
        mData.add(new PolicyEntry("Personal Information", "We do not collect, store, or share any personal information about our users. User identities remain anonymous. If we inadvertently receive any personal information, we will not disclose or share it with third parties."));
        mData.add(new PolicyEntry("Permissions", "AppVaultX requires the following permissions to deliver its features:" +
                "\n\uD83D\uDD10 moe.shizuku.manager.permission.API_V23: Permission required to use Shizuku’s privileged APIs." +
                "\n\uD83D\uDCF1 QUERY_ALL_PACKAGES: Allows AppVaultX to view the list of all installed apps and perform app-related management actions." +
                "\n\uD83D\uDCC2 WRITE_EXTERNAL_STORAGE: Allows AppVaultX to save APKs and app icons to device storage (applicable only on devices running Android versions below SDK 29 / Android 10)."));
        mData.add(new PolicyEntry("Contact Us", "If you have questions or concerns about this Privacy Policy, please contact us at: smartpack.org@gmail.com"));
        mData.add(new PolicyEntry("Changes to This Policy", "We may update this policy from time to time. Changes will be posted here."));
        return mData;
    }

    public static String getAppTheme(Context context) {
        int appTheme = Utils.getInt("appTheme", 0, context);
        switch (appTheme) {
            case 2:
                return context.getString(R.string.app_theme_light);
            case 1:
                return context.getString(R.string.app_theme_dark);
            default:
                return context.getString(R.string.app_theme_auto);
        }
    }

    public static String getShizukuMode(Context context) {
        if (isShizukuIgnored(context)) {
            return context.getString(R.string.shizuku_mode_ignored);
        } else if (Shizuku.pingBinder() && Shizuku.getVersion() >= 11) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                return context.getString(R.string.shizuku_mode_enabled);
            } else {
                return context.getString(R.string.shizuku_mode_disabled);
            }
        } else {
            return context.getString(R.string.shizuku_mode_unavailable);
        }
    }

    public static String[] getAppThemeMenu(Context context) {
        return new String[] {
                context.getString(R.string.app_theme_auto),
                context.getString(R.string.app_theme_dark),
                context.getString(R.string.app_theme_light)
        };
    }

    public static String[] getShizkuModeMenu(Context context) {
        return new String[] {
                context.getString(R.string.shizuku_mode_enabled),
                context.getString(R.string.shizuku_mode_ignored)
        };
    }

    public static void initializeAppTheme(Context context) {
        int appTheme = Utils.getInt("appTheme", 0, context);
        switch (appTheme) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        context.setTheme(R.style.AppTheme);
    }

    public static void navigateToFragment(int position, Activity activity) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(position);
    }

    public static void setSlideInAnimation(final View viewToAnimate, int position) {
        // Only animate items appearing for the first time
        if (position > -1) {
            viewToAnimate.setTranslationY(50f);
            viewToAnimate.setAlpha(0f);

            viewToAnimate.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(150)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            // Reset properties to ensure recycled views are displayed correctly
            viewToAnimate.setTranslationY(0f);
            viewToAnimate.setAlpha(1f);
        }
    }

}