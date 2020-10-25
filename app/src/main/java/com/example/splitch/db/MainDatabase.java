package com.example.splitch.db;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.splitch.db.entities.Person;
import com.example.splitch.db.entities.Product;
import com.example.splitch.db.entities.ProductPersonCrossRef;

@Database(entities = {Person.class, Product.class, ProductPersonCrossRef.class}, version = 9, exportSchema = false)
public abstract class MainDatabase extends RoomDatabase {

    private static volatile MainDatabase INSTANCE;

    private static final String TAG = MainDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "splitch";

    public abstract MainDao mainDao();

    public static MainDatabase getInstance(Context context){
        Log.e(TAG,"start method");
        if(INSTANCE == null){
            Log.e(TAG,"if null statement");
            synchronized (MainDatabase.class){
                if (INSTANCE == null){
                    Log.e(TAG,"creating method");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MainDatabase.class, DATABASE_NAME)
                            //this method delete old database when migrating to new version
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

}
