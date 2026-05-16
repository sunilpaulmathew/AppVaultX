package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public class AppOpsMenuEntry implements Serializable {

    private final String titleText, descriptionText;
    private final int id;

    public AppOpsMenuEntry(String titleText, String descriptionText, int id) {
        this.titleText = titleText;
        this.descriptionText = descriptionText;
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public String getDescription() {
        return descriptionText;
    }

    public String getTile() {
        return titleText;
    }
}