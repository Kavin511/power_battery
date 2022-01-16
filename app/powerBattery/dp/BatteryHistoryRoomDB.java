package com.power.powerBattery.dp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {BatteryHistoryDB.class}, version = 1, exportSchema = false)
public abstract class BatteryHistoryRoomDB extends RoomDatabase {
    @NonNull
    public abstract BatteryHistoryDAO batteryHistoryDAO();
    private static volatile BatteryHistoryRoomDB INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor =
    Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static BatteryHistoryRoomDB getDatabase(@NonNull final Context context) {
        if (INSTANCE == null) {
            synchronized (BatteryHistoryRoomDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),

                            BatteryHistoryRoomDB.class, "BatteryHistoryDB")
                            .build();
                }
            }
        }

        return INSTANCE;
    }
// --Commented out by Inspection START (02-11-2020 17:42):
//    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onOpen(@NonNull SupportSQLiteDatabase db) {
//            super.onOpen(db);
//
//            // If you want to keep data through app restarts,
//            // comment out the following block
////            databaseWriteExecutor.execute(() -> {
////                // Populate the database in the background.
////                // If you want to start with more words, just add them.
////                BatteryHistoryDAO dao = INSTANCE.BatteryHistoryDAO();
////                dao.deleteAll();
////
////                BatteryHistoryDB word = new BatteryHistoryDB("Hello");
////                dao.insert(word);
////                word = new BatteryHistoryDB("World");
////                dao.insert(word);
////            });
//        }
//    };
// --Commented out by Inspection STOP (02-11-2020 17:42)

}
