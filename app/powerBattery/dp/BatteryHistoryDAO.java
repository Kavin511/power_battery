package com.power.powerBattery.dp;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
@Dao
public interface BatteryHistoryDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BatteryHistoryDB batteryHistoryDB);

    @Query("DELETE FROM BatteryHistoryDB")
    void deleteAll();

    @NonNull
    @Query("SELECT * from BatteryHistoryDB order by timeStamp DESC")
   LiveData< List<BatteryHistoryDB>> getBatteryData();
}
