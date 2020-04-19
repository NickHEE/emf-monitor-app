package com.example.emf_monitor.SQL_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class EMFMonitorDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EMFMonitor.db";


    public static final int UID_INDEX = 0;
    public static final int USERNAME_INDEX = 1;
    public static final int PASSWORD_INDEX = 2;
    public static final int UNITS_INDEX = 3;
    public static final int ALARM_THRESHOLD_INDEX = 4;
    public static final int CAN_WORK_INDEX = 5;

    public static final String SQL_CREATE_TABLE_USERS = String.format("CREATE TABLE %s (\n%s INTEGER PRIMARY KEY AUTOINCREMENT\n                                                      NOT NULL,\n%s STRING  NOT NULL\n                                                      UNIQUE,\n%s STRING  NOT NULL,\n%s STRING  DEFAULT mG\n                                                      NOT NULL,\n%s DOUBLE  DEFAULT (100.0), \n%s INTEGER DEFAULT (1) \n);", UserContract.UserEntry.TABLE_NAME, UserContract.UserEntry.COLUMN_NAME_UID, UserContract.UserEntry.COLUMN_NAME_USERNAME, UserContract.UserEntry.COLUMN_NAME_PASSWORD, UserContract.UserEntry.COLUMN_NAME_UNITS, UserContract.UserEntry.COLUMN_NAME_ALARM_THRESHOLD, UserContract.UserEntry.COLUMN_NAME_CAN_WORK);

    public static final String SQL_POPULATE_TABLE_USERS = "INSERT INTO " + UserContract.UserEntry.TABLE_NAME + " (\n" +
            UserContract.UserEntry.COLUMN_NAME_USERNAME + ", " + UserContract.UserEntry.COLUMN_NAME_PASSWORD + ")\n" +
            "VALUES ('test', 'test'), ('nick', 'hi'), ('joseph', 'potato');";

    public static final String SQL_CREATE_TABLE_DATA = "CREATE TABLE " + DataContract.DataEntry.TABLE_NAME + "(\n" +
            DataContract.DataEntry.COLUMN_NAME_DID +  " INTEGER  PRIMARY KEY AUTOINCREMENT\n" +
            "                                           NOT NULL,\n" +
            DataContract.DataEntry.COLUMN_NAME_UID +  " INTEGER  REFERENCES users (UID) \n" +
            "                                           NOT NULL,\n" +
            DataContract.DataEntry.COLUMN_NAME_DATA + " STRING   NOT NULL,\n" +
            DataContract.DataEntry.COLUMN_NAME_START + " DATETIME NOT NULL,\n" +
            DataContract.DataEntry.COLUMN_NAME_STOP + " DATETIME NOT NULL" +
            ");";


    public EMFMonitorDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_DATA);
        db.execSQL(SQL_POPULATE_TABLE_USERS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public final class UserContract {

        public class UserEntry implements BaseColumns {

            public static final String TABLE_NAME = "users";
            public static final String COLUMN_NAME_UID = "UID";
            public static final String COLUMN_NAME_USERNAME = "username";
            public static final String COLUMN_NAME_PASSWORD = "password";
            public static final String COLUMN_NAME_UNITS = "units";
            public static final String COLUMN_NAME_ALARM_THRESHOLD = "alarm_threshold";
            public static final String COLUMN_NAME_CAN_WORK = "can_work";

        }
    }

    public final class DataContract {

        public class DataEntry implements BaseColumns {

            public static final String TABLE_NAME = "data";
            public static final String COLUMN_NAME_DID = "DID";
            public static final String COLUMN_NAME_UID = "UID";
            public static final String COLUMN_NAME_DATA = "data";
            public static final String COLUMN_NAME_START = "start";
            public static final String COLUMN_NAME_STOP = "stop";

        }
    }

}

