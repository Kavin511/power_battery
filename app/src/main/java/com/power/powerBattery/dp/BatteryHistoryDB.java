package com.power.powerBattery.dp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "BatteryHistoryDB")
public class BatteryHistoryDB {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "allData")
    private final String allData;
    private final long timeStamp;

    public BatteryHistoryDB(@NonNull String allData,long timeStamp)
    {
        this.allData=allData;
        this.timeStamp=timeStamp;

    }
    @NonNull
    public String getAllData()
    {
        return allData;
    }
    public long getTimeStamp()
    {
        return timeStamp;
    }
}

