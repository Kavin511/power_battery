package com.power.powerBattery.dp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BatteryHistoryRepository {
    @NonNull
    private final BatteryHistoryDAO batteryHistoryDAO;
    @NonNull
    private final LiveData<List<BatteryHistoryDB>> batteryHistoryAllData;
    BatteryHistoryRepository(@NonNull Application application)
    {
        BatteryHistoryRoomDB db=BatteryHistoryRoomDB.getDatabase(application);
        batteryHistoryDAO=db.batteryHistoryDAO();
        batteryHistoryAllData=batteryHistoryDAO.getBatteryData();
    }
    @NonNull
    LiveData<List<BatteryHistoryDB>> getBatteryHistoryAllData()
    {

        return batteryHistoryAllData;
    }
    void insert(BatteryHistoryDB batteryHistoryDB)
    {
        BatteryHistoryRoomDB.databaseWriteExecutor.execute(()-> batteryHistoryDAO.insert(batteryHistoryDB));
    }
    void deleteAll()
    {
        BatteryHistoryRoomDB.databaseWriteExecutor.execute(batteryHistoryDAO::deleteAll);
    }


}
