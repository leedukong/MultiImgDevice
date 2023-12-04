package com.releasetech.multidevice.ManagerSettings.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.Order;
import com.releasetech.multidevice.Database.DessertDataLoader;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.DatePickerFragment;
import com.releasetech.multidevice.Tool.Utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class StatisticsFragment extends Fragment {
    private static final String TAG = "[STATISTICS PAGE]";

    ArrayList<Rank> ranks = new ArrayList<>();
    ArrayList<Order> orders = new ArrayList<>();
    boolean flip = false;
    HashMap<Integer, Integer> revenueByTime = new HashMap<>();
    HashMap<String, Integer> revenueByDate = new HashMap<>();
    private RankAdapter rankAdapter = null;
    private LineChart timeChart;
    private LineChart dateChart;

    private TextView totalCountTextView;
    private TextView totalRevenueTextView;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        for (int i = 0; i < 24; i++) {
            revenueByTime.put(i, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.logD(TAG, "화면 표시됨");

        totalCountTextView = view.findViewById(R.id.statistics_total_count);
        totalRevenueTextView = view.findViewById(R.id.statistics_total_revenue);

        timeChart = view.findViewById(R.id.statistics_time_chart);
        dateChart = view.findViewById(R.id.statistics_date_chart);
        LineChart[] charts = {timeChart, dateChart};
        for (LineChart chart : charts) {
            chart.getDescription().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(false);
            chart.setScaleEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setHighlightPerDragEnabled(false);
            chart.setPinchZoom(false);
            chart.setBackgroundColor(Color.WHITE);
            chart.animateX(1500);
            chart.setExtraBottomOffset(12);
            chart.setExtraRightOffset(20);
        }

        rankAdapter = new RankAdapter(ranks);
        RecyclerView leaderboardRecyclerView = view.findViewById(R.id.leaderboard);
        leaderboardRecyclerView.setAdapter(rankAdapter);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        RadioButton todayButton = view.findViewById(R.id.statistics_today);
        RadioButton weekButton = view.findViewById(R.id.statistics_week);
        RadioButton monthButton = view.findViewById(R.id.statistics_month);
        RadioButton customDateButton = view.findViewById(R.id.statistics_custom);
        todayButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), false, false));
                dbManager.close();
                updateStatistics();
                rankAdapter.notifyDataSetChanged();
                flip = true;
            }
        });
        weekButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime past = now.minusDays(7);
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, past.getYear(), past.getMonthValue(), past.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), false, false));
                dbManager.close();
                updateStatistics();
                rankAdapter.notifyDataSetChanged();
                flip = true;
            }
        });
        monthButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime past = now.minusDays(30);
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, past.getYear(), past.getMonthValue(), past.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), false, false));
                dbManager.close();
                updateStatistics();
                rankAdapter.notifyDataSetChanged();
                flip = true;
            }
        });
        customDateButton.setOnCheckedChangeListener((compoundButton, b) -> {
            view.findViewById(R.id.statistics_start_date).setEnabled(b);
            view.findViewById(R.id.statistics_end_date).setEnabled(b);
            view.findViewById(R.id.statistics_custom_apply).setEnabled(b);
        });

        EditText startDateEditText = view.findViewById(R.id.statistics_start_date);
        EditText endDateEditText = view.findViewById(R.id.statistics_end_date);
        LocalDateTime tempNow = LocalDateTime.now();
        startDateEditText.setText(tempNow.getYear() + "-" + tempNow.getMonthValue() + "-" + tempNow.getDayOfMonth());
        endDateEditText.setText(tempNow.getYear() + "-" + tempNow.getMonthValue() + "-" + tempNow.getDayOfMonth());
        startDateEditText.setOnClickListener(view1 -> {
            String[] startSplit = startDateEditText.getText().toString().split("-");
            int startYear = Integer.parseInt(startSplit[0]);
            int startMonth = Integer.parseInt(startSplit[1]);
            int startDay = Integer.parseInt(startSplit[2]);
            DatePickerFragment datePickerFragment = new DatePickerFragment(startYear, startMonth, startDay);
            datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            datePickerFragment.setOnDateSetListener((datePicker, year, month, day) -> {
                String[] endSplit = endDateEditText.getText().toString().split("-");
                int endYear = Integer.parseInt(endSplit[0]);
                int endMonth = Integer.parseInt(endSplit[1]);
                int endDay = Integer.parseInt(endSplit[2]);
                if ((endYear < year) || ((endYear == year) && endMonth < month) || ((endYear == year) && (endMonth == month) && endDay < day)) {
                    Utils.timedAlert(getContext(), "시작일이 종료일보다\n클 수 없습니다.", 2);
                } else {
                    startDateEditText.setText(year + "-" + month + "-" + day);
                }
            });
        });
        endDateEditText.setOnClickListener(view1 -> {
            String[] endSplit = endDateEditText.getText().toString().split("-");
            int endYear = Integer.parseInt(endSplit[0]);
            int endMonth = Integer.parseInt(endSplit[1]);
            int endDay = Integer.parseInt(endSplit[2]);
            DatePickerFragment datePickerFragment = new DatePickerFragment(endYear, endMonth, endDay);
            datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            datePickerFragment.setOnDateSetListener((datePicker, year, month, day) -> {
                String[] startSplit = startDateEditText.getText().toString().split("-");
                int startYear = Integer.parseInt(startSplit[0]);
                int startMonth = Integer.parseInt(startSplit[1]);
                int startDay = Integer.parseInt(startSplit[2]);
                if ((year < startYear) || ((year == startYear) && month < startMonth) || ((year == startYear) && (month == startMonth) && day < startDay)) {
                    Utils.timedAlert(getContext(), "종료일이 시작일보다\n작을 수 없습니다.", 2);
                } else {
                    endDateEditText.setText(year + "-" + month + "-" + day);
                }
            });
        });

        Button applyCustomDateButton = view.findViewById(R.id.statistics_custom_apply);
        applyCustomDateButton.setOnClickListener(view1 -> {
            DBManager dbManager = new DBManager(getContext());
            dbManager.open();
            dbManager.create();
            String[] startSplit = startDateEditText.getText().toString().split("-");
            int startYear = Integer.parseInt(startSplit[0]);
            int startMonth = Integer.parseInt(startSplit[1]);
            int startDay = Integer.parseInt(startSplit[2]);
            String[] endSplit = endDateEditText.getText().toString().split("-");
            int endYear = Integer.parseInt(endSplit[0]);
            int endMonth = Integer.parseInt(endSplit[1]);
            int endDay = Integer.parseInt(endSplit[2]);
            Utils.logD(TAG, startYear + "/" + startMonth + "/" + startDay);
            Utils.logD(TAG, endYear + "/" + endMonth + "/" + endDay);
            orders.clear();
            orders.addAll(DessertDataLoader.loadOrders(dbManager, startYear, startMonth, startDay, endYear, endMonth, endDay, false, false));
            dbManager.close();
            updateStatistics();
            rankAdapter.notifyDataSetChanged();
            flip = true;
        });

        todayButton.setChecked(true);

        for (LineChart chart : charts) {
            chart.getLegend().setEnabled(false);
            chart.getAxisRight().setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setTextSize(16f);
            xAxis.setTextColor(R.color.black);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(true);
            xAxis.setAxisLineWidth(2f);


            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setTextColor(R.color.black);
            leftAxis.setTextSize(16f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawGridLines(true);
            leftAxis.setDrawAxisLine(true);
            leftAxis.setAxisLineWidth(2f);
            leftAxis.setGranularityEnabled(false);
        }
    }

    void updateStatistics() {
        int totalRevenue = 0;
        HashMap<String, Rank> tempRankMap = new HashMap<>();
        revenueByTime.clear();
        for (int i = 0; i < 24; i++) {
            revenueByTime.put(i, 0);
        }
        revenueByDate.clear();
        int totalCount = orders.size();
        for (Order order : orders) {
            if (!tempRankMap.containsKey(order.product))
                tempRankMap.put(order.product, new Rank(order.product, 0, 0, 0));
            Rank tempRank = tempRankMap.get(order.product);
            tempRank.revenue += order.price;
            tempRank.count++;

            LocalTime dateTime = LocalTime.parse(order.time, DateTimeFormatter.ISO_LOCAL_TIME);
            int hour = dateTime.getHour();
            revenueByTime.computeIfPresent(hour, (h, r) -> r + order.price);

            String dateString = String.format("%d-%02d-%02d", order.year, order.month, order.day);
            if (!revenueByDate.containsKey(dateString)) revenueByDate.put(dateString, 0);
            revenueByDate.computeIfPresent(dateString, (d, r) -> r + order.price);

            totalRevenue += order.price;
        }
        tempRankMap.forEach((s, rank) -> rank.percentage = rank.count * 100 / totalCount);

        Object[] tempRanksArray =
                tempRankMap.values().stream()
                        .sorted(Comparator.comparing(Rank::getCount).reversed())
                        .toArray();

        ranks.clear();
        for (Object o : tempRanksArray) {
            ranks.add((Rank) o);
        }

        for (int i = tempRanksArray.length; i < 6; i++) {
            ranks.add(new Rank("", 0, 0, 0));
        }

        setData();

        totalCountTextView.setText(totalCount + " 잔");
        totalRevenueTextView.setText(String.format("%,d 원", totalRevenue));
    }

    private void setData() {
        ArrayList<Entry> timeValues = new ArrayList<>();
        revenueByTime.forEach((h, r) -> timeValues.add(new Entry(h, r)));
        LineDataSet timeSet;

        if (timeChart.getData() != null &&
                timeChart.getData().getDataSetCount() > 0) {
            timeSet = (LineDataSet) timeChart.getData().getDataSetByIndex(0);
            timeSet.setValues(timeValues);
            YAxis leftAxis = timeChart.getAxisLeft();
            leftAxis.setAxisMaximum((float) revenueByTime.values().stream().mapToInt(x -> x).max().orElse(0) * 1.2f);
            leftAxis.setAxisMinimum(0f);
            timeChart.getData().notifyDataChanged();
            timeChart.notifyDataSetChanged();
            timeChart.invalidate();
            timeChart.animateX(1500);
        } else {
            // create a dataset and give it a type
            timeSet = new LineDataSet(timeValues, "");

            timeSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            timeSet.setColor(R.color.phthalo_green);
            timeSet.setCircleColor(R.color.black);
            timeSet.setLineWidth(3f);
            timeSet.setCircleRadius(5f);
            timeSet.setFillAlpha(65);
            timeSet.setFillColor(R.color.phthalo_green);
            timeSet.setDrawCircleHole(false);

            LineData data = new LineData(timeSet);
            data.setValueTextColor(R.color.black);
            data.setValueTextSize(16f);

            // set data
            timeChart.setData(data);
        }

        ArrayList<Entry> dateValues = new ArrayList<>();
        ArrayList<String> dateXAxis = new ArrayList<>();
        int index = 0;
        for (String key : revenueByDate.keySet().stream().sorted().collect(Collectors.toList())) {
            int revenue = revenueByDate.get(key);
            dateValues.add(new Entry(index, revenue));
            dateXAxis.add(key);
            index++;
        }
        LineDataSet dateSet;

        if (dateChart.getData() != null &&
                dateChart.getData().getDataSetCount() > 0) {
            dateSet = (LineDataSet) dateChart.getData().getDataSetByIndex(0);
            dateSet.setValues(dateValues);
            YAxis leftAxis = dateChart.getAxisLeft();
            leftAxis.setAxisMaximum((float) revenueByDate.values().stream().mapToInt(x -> x).max().orElse(0) * 1.2f);
            leftAxis.setAxisMinimum(0f);
            dateChart.getData().notifyDataChanged();
            dateChart.notifyDataSetChanged();
            dateChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateXAxis));
            dateChart.getXAxis().setCenterAxisLabels(false);
            dateChart.invalidate();
            dateChart.animateX(1500);
        } else {
            // create a dataset and give it a type
            dateSet = new LineDataSet(dateValues, "");

            dateSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dateSet.setColor(R.color.lemon_curry);
            dateSet.setCircleColor(R.color.black);
            dateSet.setLineWidth(3f);
            dateSet.setCircleRadius(5f);
            dateSet.setFillAlpha(65);
            dateSet.setFillColor(R.color.lemon_curry);
            dateSet.setDrawCircleHole(false);

            LineData data = new LineData(dateSet);
            data.setValueTextColor(R.color.black);
            data.setValueTextSize(16f);

            // set data
            dateChart.setData(data);
        }
    }

    class Rank {
        String name;
        int revenue;
        int percentage;
        int count;

        public Rank(String name, int revenue, int percentage, int count) {
            this.name = name;
            this.revenue = revenue;
            this.percentage = percentage;
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {
        private final ArrayList<Rank> mList;

        public RankAdapter(ArrayList<Rank> mList) {
            this.mList = mList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.rank_n, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Rank rank = mList.get(position);
            if (position == 0) {
                holder.layout.setBackgroundResource(R.drawable.bg_rank_1);
                ViewGroup.LayoutParams layoutParams = holder.layout.getLayoutParams();
                layoutParams.height = 100;
                holder.layout.setLayoutParams(layoutParams);
                holder.ratio.setProgressDrawable(requireContext().getDrawable(R.drawable.statistics_rank_1_progress_bar));
            } else {
                holder.layout.setBackgroundResource(R.drawable.bg_rank_n);
                ViewGroup.LayoutParams layoutParams = holder.layout.getLayoutParams();
                layoutParams.height = 85;
                holder.layout.setLayoutParams(layoutParams);
                holder.ratio.setProgressDrawable(requireContext().getDrawable(R.drawable.statistics_rank_n_progress_bar));
            }
            holder.number.setText(Integer.toString(position + 1));
            holder.name.setText(rank.name);
            holder.ratio.setProgress(rank.percentage);
            holder.revenue.setText(String.format("₩%,d", rank.revenue));
            holder.percentage.setText(rank.percentage + "%");
            holder.count.setText(Integer.toString(rank.count));
            if (rank.count >= 1000) {
                holder.count.setTextSize(22);
            } else {
                holder.count.setTextSize(26);
            }
        }


        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout layout;
            TextView number;
            TextView name;
            ProgressBar ratio;
            TextView revenue;
            TextView percentage;
            TextView count;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.rank_n);
                number = itemView.findViewById(R.id.rank_n_number);
                name = itemView.findViewById(R.id.rank_n_name);
                ratio = itemView.findViewById(R.id.rank_n_ratio);
                revenue = itemView.findViewById(R.id.rank_n_revenue);
                percentage = itemView.findViewById(R.id.rank_n_percentage);
                count = itemView.findViewById(R.id.rank_n_count);
            }

        }


    }
}

/* todo in far future
    에러 발생 데이터베이스 + 통계
    각각의 부품 누적 작동 시간 기록
 */