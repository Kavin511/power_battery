package com.power.powerBattery.dp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BatteryHistoryViewModel extends AndroidViewModel {
    @NonNull
    public final BatteryHistoryRepository batteryHistoryRepository;
    public final LiveData<List<BatteryHistoryDB>> batteryHistoryAllData;
    public BatteryHistoryViewModel(@NonNull Application application) {
        super(application);
        batteryHistoryRepository=new BatteryHistoryRepository(application);
        batteryHistoryAllData=batteryHistoryRepository.getBatteryHistoryAllData();
    }
   public LiveData<List<BatteryHistoryDB>> getBatteryHistoryAllData()
    {
        return batteryHistoryAllData;
    }
    public void insert(BatteryHistoryDB batteryHistoryDB)
    {
        batteryHistoryRepository.insert(batteryHistoryDB);
    }
// --Commented out by Inspection START (02-11-2020 17:42):
//    public void deleteAll()
//    {
//        batteryHistoryRepository.deleteAll();
//    }
// --Commented out by Inspection STOP (02-11-2020 17:42)
}
