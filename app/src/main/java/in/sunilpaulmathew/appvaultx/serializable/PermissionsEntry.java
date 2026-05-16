package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PermissionsEntry implements Serializable {

    private final boolean granted;
    private final String name;
    private String status;

    public PermissionsEntry(String name, boolean granted) {
        this.name = name;
        this.granted = granted;
        this.status = null;
    }

    public PermissionsEntry(String name, String status, boolean granted) {
        this.name = name;
        this.status = status;
        this.granted = granted;
    }

    public boolean isGranted() {
        return this.granted;
    }

    public String getPermission() {
        return this.name;
    }

    public String getPermissionName() {
        Pattern pattern = Pattern.compile("([A-Z_]+)$");
        Matcher matcher = pattern.matcher(this.name);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}