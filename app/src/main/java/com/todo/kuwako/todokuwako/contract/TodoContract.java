package com.todo.kuwako.todokuwako.contract;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by kuwako on 2016/01/12.
 */
public final class TodoContract {
    public TodoContract() {
    }

    public static abstract class Todos implements BaseColumns {
        public static final String TABLE_NAME          = "todos";
        public static final String COL_TASK            = "task"; // タスク名
        public static final String COL_IS_DONE         = "is_done"; // 完了フラグ
        public static final String COL_DEADLINE        = "deadline"; // 期限
        public static final String COL_IS_SNOOZE       = "is_snooze"; // スヌーズするか
        public static final String COL_SNOOZE_INTERVAL = "snooze_interval"; // スヌーズのインターバル
        public static final String COL_LABEL           = "label"; // label
        public static final String COL_LEVEL           = "level"; // level
        public static final String COL_STATUS          = "status"; // status
        public static final String COL_CREATED_AT      = "created_at"; // 登録日時
        public static final String COL_COMPLETED_AT    = "completed_at"; // 完了日時
    }
}
