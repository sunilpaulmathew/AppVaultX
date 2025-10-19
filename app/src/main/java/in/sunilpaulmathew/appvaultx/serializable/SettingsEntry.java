package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class SettingsEntry implements Serializable {

    private final boolean mEnabled, mSwitch;
    private final int mIcon, mPosition;
    private final String mTitle;
    private String mDescription;

    public SettingsEntry(String title) {
        this.mPosition = 0;
        this.mIcon = Integer.MIN_VALUE;
        this.mTitle = title;
        this.mDescription = null;
        this.mSwitch = false;
        this.mEnabled = false;
    }

    public SettingsEntry(int position, int icon, String title, String description) {
        this.mPosition = position;
        this.mIcon = icon;
        this.mTitle = title;
        this.mDescription = description;
        this.mSwitch = false;
        this.mEnabled = false;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isSwitch() {
        return mSwitch;
    }

    public int getIcon() {
        return mIcon;
    }

    public int getPosition() {
        return mPosition;
    }

    public String geTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

}