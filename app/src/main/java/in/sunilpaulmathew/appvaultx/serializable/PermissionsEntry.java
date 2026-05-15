package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PermissionsEntry implements Serializable {

    private final boolean granted;
    private final String name;

    public PermissionsEntry(String name, boolean granted) {
        this.name = name;
        this.granted = granted;
    }

    public boolean isGranted() {
        return this.granted;
    }

    public String getPermissionName() {
        return this.name;
    }

}