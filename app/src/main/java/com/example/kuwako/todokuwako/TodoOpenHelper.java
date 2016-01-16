package com.example.kuwako.todokuwako;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kuwako on 2016/01/12.
 */
public class TodoOpenHelper extends SQLiteOpenHelper {

//    public TodoOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, name, factory, version);
//    }
    public static final String DB_NAME = "myapp.db";
    public static final Integer DB_VERSION = 1;
    public static final String CREATE_TABLE =
            "create table " + TodoContract.Todos.TABLE_NAME + " (" +
                TodoContract.Todos._ID + " integer primary key autoincrement, " +
                TodoContract.Todos.COL_TASK + " text, " +
                TodoContract.Todos.COL_IS_DONE + " integer, " +
                TodoContract.Todos.COL_CREATED_AT + " string)";

    public static final String INIT_TABLE =
            "insert into " + TodoContract.Todos.TABLE_NAME + " (" +
                    TodoContract.Todos.COL_TASK + ", " +
                    TodoContract.Todos.COL_IS_DONE + ", " +
                    TodoContract.Todos.COL_CREATED_AT + ") values " +
                    "('SampleTask1', 0, '2016-10-10'), " +
                    "('SampleTask2', 0, '2016-01-01') ," +
                    "('SampleTask3', 1, '2016-02-28')";

    public static final String DROP_TABLE = "drop table if exsits " + TodoContract.Todos.TABLE_NAME;

    public TodoOpenHelper (Context c) {
        super(c, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL(CREATE_TABLE);
        // init table
        db.execSQL(INIT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop
        db.execSQL(DROP_TABLE);

        // onCreate
        onCreate(db);
    }
}
