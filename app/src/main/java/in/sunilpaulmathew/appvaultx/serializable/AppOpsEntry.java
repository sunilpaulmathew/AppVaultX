package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 18, 2026
 */
public class AppOpsEntry implements Serializable {

    private final boolean granted;
    private final String name;
    private String status;

    public AppOpsEntry(String name, String status, boolean granted) {
        this.name = name;
        this.status = status;
        this.granted = granted;
    }

    public boolean isGranted() {
        return this.granted;
    }

    public String getName() {
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}