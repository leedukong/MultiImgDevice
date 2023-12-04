package com.releasetech.multidevice.ManagerSettings.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.releasetech.multidevice.Log.LogService;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogsFragment extends Fragment {
    private static final String TAG = "[LOG PAGE]";

    public LogsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.logD(TAG, "화면 표시됨");

        //textview
        TextView logLastError = view.findViewById(R.id.log_last_error);
        logLastError.setText(
                        PreferenceManager.getString(getContext(), "lastErrorTime") +
                        '\n' +
                        PreferenceManager.getString(getContext(), "lastErrorMessage") +
                        "  " +
                        PreferenceManager.getString(getContext(), "lastIceErrorMessage")
        );


        List<File> logs;
        File[] tempFileList = LogService.loadLogs();
        if (tempFileList == null) {
            TextView logTextView = view.findViewById(R.id.log);
            logTextView.setText("로그가 없습니다.");
            ScrollView logScrollView = view.findViewById(R.id.log_scroll_view);
            logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
            return;
        }
        File logFile;
        logs = Arrays.asList(tempFileList);
        Collections.sort(logs);
        logFile = logs.get(0);
        StringBuilder strBuffer = new StringBuilder();
        try {
            InputStream is = new FileInputStream(logFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line).append("\n");
            }
            reader.close();
            is.close();
            TextView logTextView = view.findViewById(R.id.log);
            logTextView.setText(strBuffer.toString());
            ScrollView logScrollView = view.findViewById(R.id.log_scroll_view);
            logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
        } catch (IOException e) {
            e.printStackTrace();
        }


        Button sendLogButton = view.findViewById(R.id.send_log);
        EditText logDescriptionEditText = view.findViewById(R.id.log_description);

        sendLogButton.setOnClickListener(view1 -> new
                Thread(() -> {
            if (!isAdded()) {
                return;
            }
            if (LogService.uploadLogsToServer(requireContext(), logDescriptionEditText.getText().toString())) {
                Utils.logD(TAG, "로그 전송 성공 " + getString(R.string.report_url));
                requireActivity().runOnUiThread(() -> Utils.timedAlert(requireContext(), "로그 전송 성공", 2));
            } else {
                Utils.logD(TAG, "로그 전송 실패 " + getString(R.string.report_url));
                requireActivity().runOnUiThread(() -> Utils.timedAlert(requireContext(), "로그 전송 실패", 2));
            }
        }).start());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}