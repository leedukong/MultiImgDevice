package com.releasetech.multidevice.ManagerSettings.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import com.releasetech.multidevice.ManagerSettings.IOViewMaster;
import com.releasetech.multidevice.ManagerSettings.ManagerSettings;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class SystemFragment extends Fragment {
    private static final String TAG = "[SYSTEM PAGE]";
    volatile static boolean pauseThread = false;
    private static ManagerSettings activity;
    private static IOViewMaster ioViewMaster;

    public SystemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (ManagerSettings) requireActivity();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        pauseThread = false;
        super.onAttach(context);
    }


    boolean resumeState = true;
    boolean pauseState = true;

    @Override
    public void onResume() {
        super.onResume();
        pauseThread = false;
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause() {
        pauseThread = true;
        super.onPause();
    }

    @Override
    public void onDetach() {
        pauseThread = true;
        super.onDetach();
    }

    @Override
    public void onStop() {
        pauseThread = true;
        super.onStop();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_system, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Utils.logD(TAG, "화면 표시됨");
        ioViewMaster = new IOViewMaster(activity, view, true, true, true, true, true);
        new Thread(() -> {
            while (true) {
                if (!pauseThread) {
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException ignored) {
                    }
                    activity.runOnUiThread(() -> {
                        if (ioViewMaster == null) {
                            return;
                        }
                    });
                }
            }
        }).start();

        Button buttonQuickSettings = view.findViewById(R.id.button_quick_settings);
        buttonQuickSettings.setOnClickListener(v -> {
            QuickSetupDialog quickSetupDialog = QuickSetupDialog.getInstance();
            quickSetupDialog.show(activity.getSupportFragmentManager(), "quickSetupDialog");
        });

    }

    public static class QuickSetupDialog extends DialogFragment implements View.OnClickListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

        public QuickSetupDialog() {
        }

        public static QuickSetupDialog getInstance() {
            return new QuickSetupDialog();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.quick_settings, container, false);
            view.findViewById(R.id.button_close).setOnClickListener(this);
            if (savedInstanceState == null) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, new QuickSettingsFragment())
                        .commit();
            }
            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            getDialog().getWindow().setLayout(1600, 850);
        }

        @Override
        public void onClick(View v) {
            dismiss();
        }

        @Override
        public boolean onPreferenceStartFragment(androidx.preference.PreferenceFragmentCompat caller, Preference pref) {
            // Instantiate the new Fragment
            final Bundle args = pref.getExtras();
            final Fragment fragment = activity.getSupportFragmentManager().getFragmentFactory().instantiate(
                    activity.getClassLoader(),
                    pref.getFragment());
            fragment.setArguments(args);
            fragment.setTargetFragment(caller, 0);
            String title = pref.getTitle().toString();
            // Replace the existing Fragment with the new Fragment
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.settings, fragment, title)
                    .addToBackStack(title)
                    .commit();
            return true;
        }

        public static class QuickSettingsFragment extends PreferenceFragmentCompat {

            @Override
            public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
                setPreferencesFromResource(R.xml.quick_preferences, rootKey);
            }
        }


    }
}