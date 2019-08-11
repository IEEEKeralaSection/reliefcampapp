package xyz.appmaker.keralarescue.Room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import xyz.appmaker.keralarescue.Room.Camp.CampNameDao;
import xyz.appmaker.keralarescue.Room.Camp.CampNames;
import xyz.appmaker.keralarescue.Room.PersonData.PersonDataDao;
import xyz.appmaker.keralarescue.Room.PersonData.PersonDataEntity;

@Database(entities = {CampNames.class, PersonDataEntity.class}, version = 4)
public abstract class CampDatabase extends RoomDatabase {
    private static CampDatabase INSTANCE;

    public abstract CampNameDao campDao();
    public abstract PersonDataDao personDataDao();

    public static CampDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CampDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CampDatabase.class, "camp_database")
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
