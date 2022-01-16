package com.power.powerBattery.dp;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.power.powerBattery.R;

import java.util.List;

public class BatteryHistoryAdapter extends RecyclerView.Adapter<BatteryHistoryAdapter.BatteryHistoryViewHolder> {

    static class BatteryHistoryViewHolder extends RecyclerView.ViewHolder{
        private final TextView batteryDataView;
        private  BatteryHistoryViewHolder(@NonNull View DataView)
        {
            super(DataView);
            batteryDataView=DataView.findViewById(R.id.textView);
        }
    }
    private final LayoutInflater mInflater;
    private List<BatteryHistoryDB> batteryAllData;
    public BatteryHistoryAdapter(Context context)
    {
        mInflater=LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public BatteryHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View DataView=mInflater.inflate(R.layout.recyclerviewitem,parent,false);
        Log.d("measure", "onCreateViewHolder: ");
        return new BatteryHistoryViewHolder(DataView);
    }
    @Override
    public void onBindViewHolder(@NonNull BatteryHistoryViewHolder holder, int position) {
        if (batteryAllData!=null)
        {
            BatteryHistoryDB current=batteryAllData.get(position);
            //TODO:change display
            holder.batteryDataView.setText(Html.fromHtml(current.getAllData()));
            String data=current.getAllData();
            if(data.charAt(0)=='<')
            {
                holder.batteryDataView.setBackgroundResource(
                        R.drawable.discharge_background);

            }
            else
            {
                holder.batteryDataView.setBackgroundResource(R.drawable.charging_background);
            }
        }
        else
        {
            holder.batteryDataView.setText(R.string.no_data);
        }
    }

    @Override
    public int getItemCount() {
        if (batteryAllData!=null)
            return batteryAllData.size();
        else
            return 0;
    }
  public void setBatteryAllData(List<BatteryHistoryDB> batteryHistoryDB)
    {
        batteryAllData=batteryHistoryDB;
        notifyDataSetChanged();
    }
}
