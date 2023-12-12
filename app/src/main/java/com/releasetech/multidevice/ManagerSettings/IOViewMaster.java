package com.releasetech.multidevice.ManagerSettings;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class IOViewMaster {
    private final SensorState[] sensorStates = new SensorState[14];
    private final SensorTemperature[] sensorTemperatures = new SensorTemperature[2];
    private final ProgressBar[] functionStates = new ProgressBar[4];
    private final ComState[] comStates = new ComState[3];

    private Spinner operationBeanSpinner;
    private Spinner operationPowderSpinner;
    private Spinner operationIceSpinner;
    private Spinner operationSyrupSpinner;
    private ErrorAdapter errorAdapter;

    private ArrayList<String> errors = new ArrayList<>();

    public IOViewMaster(Context context, View view, boolean operation, boolean function, boolean sensor, boolean com, boolean error) {
        if (operation) initializeOperationFrame(context, view);
        if (function) initializeFunctionFrame(context, view);
        if (sensor) initializeSensorFrame(context, view);
        if (com) initializeComFrame(context, view);
        if (error) initializeErrorFrame(context, view);
    }

    private void initializeOperationFrame(Context context, View view) {

        operationBeanSpinner = view.findViewById(R.id.operation_bean_spinner);
        operationPowderSpinner = view.findViewById(R.id.operation_powder_spinner);
        operationIceSpinner = view.findViewById(R.id.operation_ice_spinner);
        operationSyrupSpinner = view.findViewById(R.id.operation_syrup_spinner);

        String[] BEANIDS = {"bean1_name", "bean2_name"};
        ArrayList<String> beanNames = new ArrayList<>();
        for (String beanid : BEANIDS) {
            beanNames.add(PreferenceManager.getString(context, beanid));
        }
        ArrayAdapter operationBeanAdapter = new ArrayAdapter(context, R.layout.spinner_item, beanNames);
        operationBeanSpinner.setAdapter(operationBeanAdapter);

        String[] POWDERIDS = {"powder1_name", "powder2_name", "powder3_name", "powder4_name", "powder5_name"};
        ArrayList<String> powderNames = new ArrayList<>();
        for (String powderid : POWDERIDS) {
            powderNames.add(PreferenceManager.getString(context, powderid));
        }
        ArrayAdapter operationPowderAdapter = new ArrayAdapter(context, R.layout.spinner_item, powderNames);
        operationPowderSpinner.setAdapter(operationPowderAdapter);

        ArrayList<String> iceNames = new ArrayList<>();
        iceNames.add("얼음");
        iceNames.add("정수");
        ArrayAdapter operationIceAdapter = new ArrayAdapter(context, R.layout.spinner_item, iceNames);
        operationIceSpinner.setAdapter(operationIceAdapter);


        String[] SYRUPIDS = {"syrup1_name", "syrup2_name", "syrup3_name", "syrup4_name", "syrup5_name"};
        ArrayList<String> syrupNames = new ArrayList<>();
        for (String syrupid : SYRUPIDS) {
            syrupNames.add(PreferenceManager.getString(context, syrupid));
        }
        ArrayAdapter operationSyrupAdapter = new ArrayAdapter(context, R.layout.spinner_item, syrupNames);
        operationSyrupSpinner.setAdapter(operationSyrupAdapter);
    }

    private void initializeFunctionFrame(Context context, View view) {
    }

    private void initializeSensorFrame(Context context, View view) {
    }

    private void initializeComFrame(Context context, View view) {
        int[] COMSTATEIDS = {R.id.com_state_io, R.id.com_state_ice, R.id.com_state_flow};
        String[] COMSTATENAMES = {"IO보드", "제빙기", "유량계"};
        for (int i = 0; i < COMSTATEIDS.length; i++) {
            comStates[i] = new ComState(view, COMSTATEIDS[i], COMSTATENAMES[i]);
        }
    }

    private void initializeErrorFrame(Context context, View view) {
        errorAdapter = new ErrorAdapter(errors);
        RecyclerView errorsRecyclerView = view.findViewById(R.id.errors);
        errorsRecyclerView.setAdapter(errorAdapter);
        errorsRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
    }

    public void setSensorState(int index, int state) {
        sensorStates[index].setState(state);
    }

    public void setSensorTemperature(int index, float temp) {
        sensorTemperatures[index].setTemp(temp);
    }

    public void setFirmwareVersion(String version) {
        sensorTemperatures[sensorTemperatures.length - 1].setText(version);
    }

    public void setFunctionState(int index, int state) {
        if (state == 0) {
            functionStates[index].setVisibility(View.INVISIBLE);
        } else {
            functionStates[index].setVisibility(View.VISIBLE);
        }
    }

    public void setComState(int index, int state) {
        comStates[index].setState(state);
    }


    private class SensorState {
        private final ImageView img;

        SensorState(View view, int id, String name) {
            ConstraintLayout layout = view.findViewById(id);
            TextView nameText = layout.findViewById(R.id.sensor_name);
            nameText.setText(name);
            img = layout.findViewById(R.id.sensor_state);
        }

        public void setState(int state) {
            img.setSelected(state > 0);
        }
    }

    private static class SensorTemperature {
        private final TextView tempText;

        SensorTemperature(View view, int id, String name) {
            ConstraintLayout layout = view.findViewById(id);
            TextView nameText = layout.findViewById(R.id.sensor_name);
            nameText.setText(name);
            tempText = layout.findViewById(R.id.sensor_temp);
        }

        public void setTemp(float temp) {
            String strTemp = String.format("%.1f°C", temp);
            tempText.setText(strTemp);
        }

        public void setText(String str) {
            tempText.setText(str);
        }
    }

    private static class ComState {
        private final ImageView img;

        ComState(View view, int id, String name) {
            ConstraintLayout layout = view.findViewById(id);
            TextView nameText = layout.findViewById(R.id.com_name);
            nameText.setText(name);
            img = layout.findViewById(R.id.com);
        }

        public void setState(int state) {
            if (state == 0) {
                img.setImageResource(R.drawable.ic_led_off);
            } else {
                img.setImageResource(R.drawable.ic_led_green);
            }
        }
    }

    public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ViewHolder> {
        private final ArrayList<String> mList;

        public ErrorAdapter(ArrayList<String> mList) {
            this.mList = mList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.error_state, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            String name = mList.get(position);
            holder.errorName.setText(name);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView errorName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                errorName = itemView.findViewById(R.id.error_name);
            }
        }


    }

}