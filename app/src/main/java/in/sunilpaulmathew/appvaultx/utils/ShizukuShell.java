package in.sunilpaulmathew.appvaultx.utils;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import in.sunilpaulmathew.appvaultx.BuildConfig;
import in.sunilpaulmathew.appvaultx.services.ShellService;
import rikka.shizuku.Shizuku;
import sunilpaulmathew.appvaultx.IShellService;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class ShizukuShell {
    private static IShellService mShellService;

    public static void ensureUserService(Runnable runnable) {
        if (mShellService != null && runnable != null) {
            runnable.run();
            return;
        }

        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(
                new ComponentName(BuildConfig.APPLICATION_ID, ShellService.class.getName()))
                .daemon(false)
                .processNameSuffix("shizuku_shell")
                .debuggable(BuildConfig.DEBUG)
                .version(BuildConfig.VERSION_CODE);

        Shizuku.bindUserService(args, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                if (binder == null || !binder.pingBinder()) return;

                mShellService = IShellService.Stub.asInterface(binder);

                if (runnable != null) {
                    new Handler(Looper.getMainLooper()).post(runnable);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mShellService = null;
            }
        });
    }

    public static String runCommand(String command) {
        if (mShellService != null) {
            try {
                return mShellService.runCommand(command);
            } catch (RemoteException ignored) {
            }
        }
        return "";
    }

    public static void runCommands(String commands) {
        if (mShellService != null) {
            try {
                mShellService.runCommands(commands);
            } catch (RemoteException ignored) {
            }
        }
    }

    public static void writeToFile(ParcelFileDescriptor data, String path) {
        if (mShellService != null) {
            try {
                mShellService.writeToFile(data, path);
            } catch (RemoteException ignored) {
            }
        }
    }

    public static void destroy() {
        try {
            if (mShellService != null) mShellService.destroyProcess();
        } catch (RemoteException ignored) {
        }
    }

}