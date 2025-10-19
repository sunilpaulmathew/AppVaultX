package in.sunilpaulmathew.appvaultx.services;

import android.os.ParcelFileDescriptor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import sunilpaulmathew.appvaultx.IShellService;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class ShellService extends IShellService.Stub {

    private static Process mProcess = null;

    @Override
    public void destroyProcess() {
        if (mProcess != null) mProcess.destroy();
    }

    @Override
    public String runCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            mProcess = Runtime.getRuntime().exec(command, null, null);
            BufferedReader mInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            BufferedReader mError = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));

            mProcess.waitFor();

            String line;
            while ((line = mInput.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = mError.readLine()) != null) {
                output.append(line).append("\n");
            }

        }
        catch (Exception ignored) {
        }
        finally {
            if (mProcess != null)
                mProcess.destroy();
        }
        return output.toString();
    }

    @Override
    public void runCommands(String commands) {
        new Thread(() -> {
            try {
                mProcess = Runtime.getRuntime().exec(
                        new String[] {
                                "sh", "-c", commands
                        }
                );

                mProcess.waitFor();
            }
            catch (Exception ignored) {
            }
            finally {
                if (mProcess != null)
                    mProcess.destroy();
            }
        }).start();
    }

    @Override
    public void writeToFile(ParcelFileDescriptor pfd, String destinationPath) {
        try (FileInputStream in = new FileInputStream(pfd.getFileDescriptor());
             FileOutputStream out = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ignored) {
        } finally {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ignored) {}
            }
        }
    }

}