package in.sunilpaulmathew.appvaultx.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.InstalledPackagesAdapter;
import in.sunilpaulmathew.appvaultx.dialogs.BatchDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class InstalledPackagesFragment extends Fragment {

    private boolean mExit;
    private BottomNavigationView navigationView;
    private ContentLoadingProgressBar progressBar;
    private LinearLayoutCompat contentLayout, headerLayout, progressLayout;
    private MaterialButton batchOptions;
    private RecyclerView recyclerView;
    private TextInputEditText editText;
    private InstalledPackagesAdapter adapter;
    private List<PackagesEntry> mPackages;
    private final List<PackagesEntry> selectedApps = new CopyOnWriteArrayList<>();
    private String mSearchText = null;
    private final Handler mHandler = new Handler();

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_packages, container, false);

        AppCompatImageButton apply = mRootView.findViewById(R.id.apply);
        navigationView = requireActivity().findViewById(R.id.bottom_navigation);
        progressBar = mRootView.findViewById(R.id.progress);
        batchOptions = mRootView.findViewById(R.id.batch_options);
        MaterialCardView searchCard = mRootView.findViewById(R.id.search_card);
        contentLayout = mRootView.findViewById(R.id.content_layout);
        headerLayout = mRootView.findViewById(R.id.header_layout);
        progressLayout = mRootView.findViewById(R.id.progress_layout);
        recyclerView = mRootView.findViewById(R.id.recycler_view);
        editText = mRootView.findViewById(R.id.search_word);
        MaterialButton searchButton = mRootView.findViewById(R.id.search);
        MaterialButton sortButton = mRootView.findViewById(R.id.sort);
        MaterialTextView title = mRootView.findViewById(R.id.title);
        TabLayout tabLayout = mRootView.findViewById(R.id.tab_layout);

        title.setText(getString(R.string.apps_installed));

        tabLayout.setVisibility(VISIBLE);
        for (String label : getLabels()) {
            tabLayout.addTab(tabLayout.newTab().setText(label));
        }
        Objects.requireNonNull(tabLayout.getTabAt(getAppType())).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int newType = tab.getPosition();
                if (newType != getAppType()) {
                    Utils.saveInt("appType", tab.getPosition(), requireActivity());
                    loadPackages(mSearchText).execute();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        searchButton.setOnClickListener(v -> {
            if (searchCard.getVisibility() == VISIBLE) {
                searchCard.setVisibility(GONE);
                editText.setText(null);
            } else {
                searchCard.setVisibility(VISIBLE);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    loadPackages(null).execute();
                    apply.setVisibility(GONE);
                } else {
                    apply.setVisibility(VISIBLE);
                }
            }
        });

        apply.setOnClickListener(view -> loadPackages(Objects.requireNonNull(editText.getText()).toString().trim()).execute());

        sortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireActivity(), v);
            Menu menu = popupMenu.getMenu();
            menu.add(0, 0, Menu.NONE, R.string.reverse).setIcon(R.drawable.ic_sort_az).setCheckable(true).setChecked(Utils.getBoolean("reverse_order", false, requireActivity()));

            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    Utils.saveBoolean("reverse_order", !Utils.getBoolean("reverse_order", false, requireActivity()), requireActivity());
                    loadPackages(mSearchText).execute();
                }
                return false;
            });
            popupMenu.show();
        });

        loadPackages(mSearchText).execute();

        batchOptions.setOnClickListener(v -> new BatchDialog(selectedApps, Utils.getDrawable(R.drawable.ic_delete, requireActivity()), getString(R.string.remove_selected, selectedApps.size()), getString(R.string.remove), v.getContext()) {
            @Override
            public void positiveButtonListener(List<String> apps) {
                removeApps(apps).execute();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchCard.getVisibility() == VISIBLE) {
                    searchCard.setVisibility(GONE);
                    editText.setText(null);
                } else if (!selectedApps.isEmpty()) {
                    selectedApps.clear();
                    loadPackages(mSearchText).execute();
                } else if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    Utils.toast(getString(R.string.press_back), requireActivity()).show();
                    mExit = true;
                    mHandler.postDelayed(() -> mExit = false, 2000);
                }
            }
        });

        return mRootView;
    }

    private Async loadPackages(String searchText) {
        return new Async() {

            @Override
            public void onPreExecute() {
                progressLayout.setVisibility(VISIBLE);
                headerLayout.setVisibility(GONE);
                contentLayout.setVisibility(GONE);
                navigationView.setVisibility(GONE);
            }

            @Override
            public void doInBackground() {
                if (Packages.getPackages() == null || Packages.getPackages().isEmpty()) {
                    Packages.loadPackages(progressBar, requireActivity());
                }

                try {
                    mPackages = new CopyOnWriteArrayList<>();
                    int appType = getAppType();
                    String query = searchText == null ? "" : searchText.toLowerCase(Locale.ROOT);

                    for (PackagesEntry pkg : Packages.getPackages()) {

                        if (!matchesAppType(appType, pkg)) continue;

                        if (!query.isEmpty() && !pkg.getPackageName().toLowerCase(Locale.ROOT).contains(query))
                            continue;

                        mPackages.add(pkg);
                    }

                    mPackages.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));

                    if (Utils.getBoolean("reverse_order", false, requireActivity())) {
                        Collections.reverse(mPackages);
                    }

                    adapter = new InstalledPackagesAdapter(mPackages, selectedApps, batchOptions, requireActivity());
                } catch (Exception ignored) {}
            }

            private boolean matchesAppType(int selectedType, PackagesEntry pkg) {
                int type = pkg.getAppType();
                String rec = pkg.getRemovalRecommendation();

                switch (selectedType) {
                    case 0:
                        // System apps
                        return type == 2 || type == 5;
                    case 1:
                        // Updated system apps
                        return type == 1 || type == 4;
                    case 2:
                        // User apps
                        return type == 0 || type == 3;
                    case 3:
                        // Disabled
                        return type == 3 || type == 4 || type == 5 || type == 6;
                    case 4:
                        // Debuggable
                        return type == 6 || type == 7;
                    case 5:
                        // Safe to remove
                        return "Recommended".equals(rec);
                    case 6:
                        // Likely safe
                        return "Advanced".equals(rec);
                    case 7:
                        // Likely dangerous
                        return "Expert".equals(rec);
                    case 8:
                        // Dangerous
                        return "Unsafe".equals(rec);
                    default:
                        return false;
                }
            }

            @SuppressLint("StringFormatMatches")
            @Override
            public void onPostExecute() {
                if (!isAdded()) return;
                progressLayout.setVisibility(GONE);
                headerLayout.setVisibility(VISIBLE);
                contentLayout.setVisibility(VISIBLE);
                navigationView.setVisibility(VISIBLE);
                mSearchText = searchText;
                editText.setHint(getString(R.string.search_hint, adapter.getItemCount()));
                recyclerView.setItemAnimator(null);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0);
                }
                recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(adapter);
            }
        };
    }

    private Async removeApps(List<String> apps) {
        return new Async() {
            private final List<Integer> positions = new CopyOnWriteArrayList<>();
            private ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(requireActivity());
                progressDialog.setProgressStatus(R.string.applying);
                progressDialog.startDialog();
            }

            @Override
            public void doInBackground() {
                String cmd = apps.stream()
                        .map(pkg -> "pm uninstall --user " + Utils.getUserID() + " " + pkg)
                        .collect(Collectors.joining("; "));
                ShizukuShell.runCommands(cmd);

                for (String packageName : apps) {
                    PackagesEntry entry = Packages.getPackages().stream().filter(i -> i.getPackageName().equals(packageName)).findFirst().orElse(null);
                    int index = IntStream.range(0, mPackages.size())
                            .filter(i -> mPackages.get(i).getPackageName().equals(packageName))
                            .findFirst()
                            .orElse(-1);
                    if (entry != null) {
                        selectedApps.remove(entry);
                        Packages.getPackages().remove(entry);
                        if (entry.isSystemApp()) {
                            Packages.getUninstalledPackages().add(new PackagesEntry(entry.getPackageName(), entry.getAPKPath(), entry.getRemovalRecommendation(), entry.getUADDescription(), entry.getAppType(), false));
                        }
                    }
                    if (index != -1) {
                        mPackages.remove(index);
                        positions.add(index);
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) return;
                progressDialog.dismissDialog();
                for (Integer position : positions) {
                    adapter.notifyItemRemoved(position);
                }
                adapter.notifyItemRangeChanged(0, mPackages.size());
            }
        };
    }


    private int getAppType() {
        return Utils.getInt("appType", 0, requireActivity());
    }

    private String[] getLabels() {
        return new  String[] {
                getString(R.string.app_type_system_title),
                getString(R.string.app_type_system_updated_title),
                getString(R.string.app_type_user_title),
                getString(R.string.app_type_disabled_title),
                getString(R.string.app_type_debuggable_title),
                getString(R.string.uad_status_recommended_title),
                getString(R.string.uad_status_advanced_title),
                getString(R.string.uad_status_expert_title),
                getString(R.string.uad_status_unsafe_title)
        };
    }

}