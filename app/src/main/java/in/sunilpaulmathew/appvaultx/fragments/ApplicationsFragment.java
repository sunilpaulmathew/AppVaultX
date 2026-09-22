package in.sunilpaulmathew.appvaultx.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.adapters.ApplicationsAdapter;
import in.sunilpaulmathew.appvaultx.dialogs.BatchDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import in.sunilpaulmathew.appvaultx.viewModels.ApplicationsViewModel;
import navView.NavView;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 07, 2026
 */
public class ApplicationsFragment extends Fragment {

    private ApplicationsAdapter adapter;
    private boolean mExit;
    private ContentLoadingProgressBar progressBar;
    private int mTabPosition = 2;
    private LinearLayoutCompat progressLayout;
    private List<PackagesEntry> mPackages;
    private NavView navigationView;
    private String mSearchText = null;
    private TextInputEditText editText;
    private final List<PackagesEntry> selectedApps = new CopyOnWriteArrayList<>();

    public ApplicationsFragment() {
    }

    public static ApplicationsFragment newInstance(int position) {
        ApplicationsFragment fragment = new ApplicationsFragment();

        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTabPosition = getArguments().getInt("tabPosition");
        }
    }

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_applications, container, false);

        AppCompatImageButton apply = mRootView.findViewById(R.id.apply);
        progressBar = mRootView.findViewById(R.id.progress);
        MaterialButton batchOptions = mRootView.findViewById(R.id.batch_options);
        MaterialCardView searchCard = mRootView.findViewById(R.id.search_card);
        progressLayout = mRootView.findViewById(R.id.progress_layout);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);
        editText = mRootView.findViewById(R.id.search_word);

        ApplicationsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ApplicationsViewModel.class);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new ApplicationsAdapter(new CopyOnWriteArrayList<>(), selectedApps, batchOptions, 0, requireActivity());
        recyclerView.setAdapter(adapter);

        navigationView = Settings.getNavView(requireActivity());

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

        viewModel.getSearchTrigger().observe(getViewLifecycleOwner(), v -> {
            if (searchCard.getVisibility() == VISIBLE) {
                searchCard.setVisibility(GONE);
                editText.setText(null);
            } else {
                searchCard.setVisibility(VISIBLE);
            }
        });

        viewModel.getSortTrigger().observe(getViewLifecycleOwner(), v -> loadPackages(mSearchText).execute());

        loadPackages(mSearchText).execute();

        batchOptions.setOnClickListener(v -> new BatchDialog(selectedApps, Utils.getDrawable(R.drawable.ic_delete, requireActivity()), getString(R.string.remove_selected, selectedApps.size()), getString(R.string.remove), v.getContext()) {
            @Override
            public void positiveButtonListener(List<String> apps) {
                removeApps(apps).execute();
            }
        });

        Settings.applyMargin(recyclerView, requireActivity());

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchCard.getVisibility() == VISIBLE) {
                    searchCard.setVisibility(GONE);
                    editText.setText(null);
                } else if (!selectedApps.isEmpty()) {
                    selectedApps.clear();
                    adapter.refreshData();
                } else if (mExit) {
                    mExit = false;
                    requireActivity().finish();
                } else {
                    Utils.toast(getString(R.string.press_back), requireActivity()).show();
                    mExit = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> mExit = false, 2000);
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
                navigationView.setVisibility(GONE);
            }

            private List<PackagesEntry> getData() {
                List<PackagesEntry> data = new CopyOnWriteArrayList<>();
                try {
                    String query = searchText == null ? "" : searchText.toLowerCase(Locale.ROOT);

                    int max = Packages.getPackages().size();
                    progressBar.setMax(max);
                    for (PackagesEntry pkg : Packages.getPackages()) {

                        if (!matchesAppType(mTabPosition, pkg)) continue;

                        if (!query.isEmpty() && !pkg.getPackageName().toLowerCase(Locale.ROOT).contains(query)) continue;

                        data.add(pkg);
                        if (progressBar.getProgress() < max) {
                            progressBar.setProgress(progressBar.getProgress() + 1);
                        } else {
                            progressBar.setProgress(0);
                        }
                    }

                    Utils.saveInt("appType", mTabPosition, requireActivity());

                    data.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPackageName(), rhs.getPackageName()));

                    if (Utils.getBoolean("reverse_order", false, requireActivity())) {
                        Collections.reverse(data);
                    }
                } catch (Exception ignored) {}
                return data;
            }

            @Override
            public void doInBackground() {
                mPackages = getData();
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
                        // Debuggable
                        return type == 6 || type == 7;
                    case 4:
                        // Safe to remove
                        return "Recommended".equals(rec);
                    case 5:
                        // Likely safe
                        return "Advanced".equals(rec);
                    case 6:
                        // Likely dangerous
                        return "Expert".equals(rec);
                    case 7:
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
                navigationView.setVisibility(VISIBLE);
                adapter.updateData(mPackages);
                editText.setHint(getString(R.string.search_hint, adapter.getItemCount()));
                mSearchText = searchText;
            }
        };
    }

    private Async removeApps(List<String> apps) {
        return new Async() {
            private  boolean failed = false;
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
                        .map(pkg -> "pm enable --user " + Utils.getUserID() + " " + pkg)
                        .collect(Collectors.joining("; "));
                ShizukuShell.runCommands(cmd);

                for (String packageName : apps) {
                    if (!Packages.isAppInstalled(packageName, requireActivity())) {
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
                        }
                    } else {
                        failed = true;
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (!isAdded()) return;
                progressDialog.dismissDialog();
                adapter.updateData(mPackages);

                if (failed) {
                    Utils.toast(R.string.remove_selected_failed, requireActivity()).show();
                }
            }
        };
    }

}