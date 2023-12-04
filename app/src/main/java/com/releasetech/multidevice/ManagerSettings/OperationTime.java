package com.releasetech.multidevice.ManagerSettings;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.releasetech.multidevice.R;

public class OperationTime {
    public SeekBar bar;
    private TextView timeText;
    private int time = 20;
    private boolean amount = false;
    private int unit = 1;
    OperationTime(View view, int barId, int textViewId) {
        bar = view.findViewById(barId);
        timeText = view.findViewById(textViewId);
        Object tag = bar.getTag(R.id.bar_type);
        if (tag != null) amount = tag.toString().equals("amount");
//        Object tag2 = bar.getTag(R.id.disabled);
//        if(tag2 !=null) bar.setEnabled(false);
        Object tag3 = bar.getTag(R.id.seekbar_unit);
        if (tag3 != null) {
            unit = Integer.parseInt(tag3.toString());
        }
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (unit > 1) {
                    progress = progress / unit;
                    progress = progress * unit;
                }
                time = progress;
                String temp;
                if (amount) {
                    temp = "" + time + "ml";
                } else {
                    temp = String.format("%.1f", ((float) time) / 10) + "초";
                }
                timeText.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public int getTime() {
        return time;
    }
}
