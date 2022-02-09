package com.power.powerBattery;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.button.MaterialButton;
import com.power.powerBattery.dp.BatteryHistoryAdapter;
import com.power.powerBattery.dp.BatteryHistoryDB;
import com.power.powerBattery.dp.BatteryHistoryViewModel;

import java.util.List;

public class stats extends Fragment {


    public stats() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    TextView battery_stats;
    TextView history_data;
    MaterialButton speed_test;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_stats, container, false);
        battery_stats=v.findViewById(R.id.battery_stats);
        history_data=v.findViewById(R.id.history_report);
        speed_test=v.findViewById(R.id.speed_test);
        battery_stats.setOnClickListener(v1 -> {
            try {
                Intent powerSummary = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                ResolveInfo resolveInfo = requireActivity().getPackageManager().resolveActivity(powerSummary, 0);
                if (resolveInfo != null) {
                    startActivity(powerSummary);
                }
                else
                {
                    new AlertDialog.Builder(getContext())
                        .setTitle("Summary not found")
                        .setMessage("Your phone does not have battery summary")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.cancel())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            speed_test.setOnClickListener(v12 -> {
                Intent intent = new Intent(getContext(), speed_test.class);
                startActivity(intent);
            });
        }
        else {
            ConstraintLayout speed=v.findViewById(R.id.speed);
            speed.setVisibility(View.GONE);
        }
        RecyclerView recyclerView = v.findViewById(R.id.recyclerview);
        final BatteryHistoryAdapter adapter = new BatteryHistoryAdapter(getContext());
        BatteryHistoryViewModel batteryHistoryViewModel = new ViewModelProvider(this).get(BatteryHistoryViewModel.class);

        batteryHistoryViewModel.getBatteryHistoryAllData().observe(requireActivity(), (List<BatteryHistoryDB> batteryHistoryDBS) -> {
            if (batteryHistoryDBS.size()==0)
            {
                recyclerView.setVisibility(View.GONE);
                history_data.setVisibility(View.VISIBLE);
            }
            else
            {
                recyclerView.setVisibility(View.VISIBLE);
                history_data.setVisibility(View.GONE);
            }
            adapter.setBatteryAllData(batteryHistoryDBS);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }
}
