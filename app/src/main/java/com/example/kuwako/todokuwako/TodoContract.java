package com.example.kuwako.todokuwako;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by kuwako on 2016/01/12.
 */
public final class TodoContract {
    public TodoContract() {};

    public static abstract class Todos implements BaseColumns {
        public static final String TABLE_NAME   = "todos";
        public static final String COL_TASK     = "task";
        public static final String COL_IS_DONE = "is_done";
        public static final String COL_CREATED_AT = "created_at";
    }
}
