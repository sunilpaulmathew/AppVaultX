package in.sunilpaulmathew.appvaultx.serializable;

import java.io.Serializable;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class PolicyEntry implements Serializable {

    private final String mTitle, mSummary;

    public PolicyEntry(String title, String summary) {
        this.mTitle = title;
        this.mSummary = summary;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSummary() {
        return mSummary;
    }

}