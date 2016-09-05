package com.example.kuwako.todokuwako;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.addButton)
    Button addButton;
    @BindView(R.id.todoListView)
    ListView todoListView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.editText)
    EditText editTask;

    private TodoListAdapter mAdapter;
    private DialogFragment mInputFragment;
    private DialogFragment mEditFragment;
    private ArrayList<Todo> mList;
    private String logTag = "@@@@@BAITALK_TAG";
    private FirebaseAnalytics mFirebaseAnalytics;
    private Todo editTodo = null;

    // TODO EventBus導入
    // TODO Dagger2導入
    // TODO DBをrealmに移行
    // TODO 関数をHelper系クラスに移行
    // TODO スヌーズ機能
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(BuildConfig.DEBUG);
        mInputFragment = new InputDialogFragment();
        mEditFragment = new EditDialogFragment();
        editTask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // キーボードを隠す処理
                if (hasFocus == false) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        mList = new ArrayList<>();
        mAdapter = new TodoListAdapter(MainActivity.this) {};
        mAdapter.setTodoArrayList(mList);

        todoListView.setAdapter(mAdapter);

        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        Cursor c = null;
        c = db.query(
                TodoContract.Todos.TABLE_NAME,
                null, // fields
                TodoContract.Todos.COL_IS_DONE + " < ?", // where
                new String[]{"1"}, // where arg
                null, // group by
                null, // having
                TodoContract.Todos.COL_CREATED_AT + " desc"// order by
        );

        while (c.moveToNext()) {
            Todo todo = new Todo();
            todo.setTask(c.getString(c.getColumnIndex(TodoContract.Todos.COL_TASK)));
            todo.setDeadline(c.getString(c.getColumnIndex(TodoContract.Todos.COL_DEADLINE)));
            todo.setId(c.getLong(c.getColumnIndex(TodoContract.Todos._ID)));

            mList.add(0, todo);
            mAdapter.notifyDataSetChanged();
        }

        mAdapter.notifyDataSetChanged();

        // TODO 削除。DB内部確認
        checkDB();

        c.close();
        db.close();

        // シングルタップでモーダル表示 + 編集削除
        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                editTodo = (Todo) listView.getItemAtPosition(position);

                mEditFragment.show(getFragmentManager(), "bbb");
            }
        });

        todoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                editTodo = (Todo) listView.getItemAtPosition(position);

                mEditFragment.show(getFragmentManager(), "bbb");
                return true;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // デフォルトだとAlarm側から起動されるintentは
        // Activityに留まったままAlarmManagerからAへの新しいIntentを複数回
        // 投げたところ、getIntet()で取得するIntentがActivity起動時のものから変わらない
        super.onNewIntent(intent);
        // 画面表示時に再度起動された際にgetIntent()を更新する。
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent.getIntExtra("todoId", 0) != 0) {
            for (int i = 0; mList.size() > i; i++) {
                Todo targetTodo = mList.get(i);

                if ((int) targetTodo.getId() == intent.getIntExtra("todoId", 0)) {
                    editTodo = targetTodo;
                    break;
                }
            }

            if (editTodo != null) {
                mEditFragment.show(getFragmentManager(), "bbb");
            }
        }
    }

    public void addList(View view) {
        String task = String.valueOf(editTask.getText());

        Todo todo = new Todo();
        todo.setTask(task);

        Time time = new Time("Asia/Tokyo");
        time.setToNow();
        editTask.setText("");

        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues newTask = new ContentValues();
        newTask.put(TodoContract.Todos.COL_TASK, task);
        newTask.put(TodoContract.Todos.COL_IS_DONE, 0);
        newTask.put(TodoContract.Todos.COL_CREATED_AT, time.year + "-" + (time.month + 1) + "-" + time.monthDay);

        long newId = db.insert(TodoContract.Todos.TABLE_NAME, null, newTask);
        todo.setId(newId);

        mList.add(0, todo);
        mAdapter.notifyDataSetChanged();
        db.close();

        Log.e(logTag, task);

        // TODO 削除 デバッグ用関数
        checkDB();
    }

    // デバッグ用
    private void checkDB() {
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        // 処理
        Cursor c = null;
        c = db.query(
                TodoContract.Todos.TABLE_NAME,
                null, // fields
                null, // where
                null, // where arg
                null, // group by
                null, // having
                TodoContract.Todos.COL_CREATED_AT + " desc"// order by
        );

        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex(TodoContract.Todos._ID));
            String task = c.getString(c.getColumnIndex(TodoContract.Todos.COL_TASK));
            int is_done = c.getInt(c.getColumnIndex(TodoContract.Todos.COL_IS_DONE));
            String created_at = c.getString(c.getColumnIndex(TodoContract.Todos.COL_CREATED_AT));
            String completed_at = c.getString(c.getColumnIndex(TodoContract.Todos.COL_COMPLETED_AT));
            String deadline = c.getString(c.getColumnIndex(TodoContract.Todos.COL_DEADLINE));

            Log.v(logTag,
                    "id: " + id +
                            " task: " + task +
                            " is_done: " + is_done +
                            " deadline: " + deadline +
                            " created_at: " + created_at +
                            " completed_at: " + completed_at
            );
        }

        c.close();
        db.close();
    }

    private boolean deleteTodo(Todo todo) {
        Toast.makeText(MainActivity.this, todo.getTask() + " is completed.", Toast.LENGTH_LONG).show();
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues updateTask = new ContentValues();
        updateTask.put(TodoContract.Todos.COL_IS_DONE, 1);

        int updateCount = db.update(
                TodoContract.Todos.TABLE_NAME,
                updateTask,
                TodoContract.Todos.COL_TASK + " = ?",
                new String[]{todo.getTask()}
        );

        db.close();

        mList.remove(todo);
        mAdapter.notifyDataSetChanged();

        // TODO デバッグ用関数。削除。
        checkDB();

        return updateCount > 0;
    }

    private boolean saveTodo(Todo todo) {
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues updateTask = new ContentValues();
        updateTask.put(TodoContract.Todos.COL_TASK, todo.getTask());
        updateTask.put(TodoContract.Todos.COL_DEADLINE, todo.getDeadline());

        int updateCount = db.update(
                TodoContract.Todos.TABLE_NAME,
                updateTask,
                TodoContract.Todos.COL_TASK + " = ?",
                new String[]{todo.getTask()}
        );

        db.close();
        mAdapter.notifyDataSetChanged();

        // TODO デバッグ用関数。削除。
        checkDB();

        return updateCount > 0;
    }

    private void setTodoAlarm(Todo todo, int alarmId, Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        intent.putExtra("task", todo.getTask());
        intent.putExtra("deadline", todo.getDeadline());
        intent.putExtra("id", alarmId);
        intent.putExtra("todoId", alarmId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), alarmId, intent, 0);
        checkDB();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        }
    }

    @OnClick({R.id.addButton, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                addList(view);
                break;
            case R.id.fab:
                mInputFragment.show(getFragmentManager(), "aaa");
                break;
        }
    }

    public class EditDialogFragment extends DialogFragment {
        @BindView(R.id.editTask)
        EditText editTask;
        @BindView(R.id.editTime)
        TextView editTime;
        @BindView(R.id.saveBtn)
        Button saveBtn;
        @BindView(R.id.deleteBtn)
        Button deleteBtn;
        private int mYear;
        private int mMonth;
        private int mDay;
        private int mHour;
        private int mMinute;
        Calendar mCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog;
        TimePickerDialog timePickerDialog;
        String deadLine;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            if (editTodo == null) {
                dismiss();
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dialog_edit, null);
            ButterKnife.bind(this, content);

            editTask.setText(editTodo.getTask());
            editTask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // キーボードを隠す処理
                    if (hasFocus == false) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            });

            // TODO そもそも期限を削除する機能も必要そう。
            if (editTodo.getDeadline() != null) {
                editTime.setText(editTodo.getDeadline());
                String[] deadLineArr = editTodo.getDeadline().split(" ", 0);
                String[] deadLineDateArr = deadLineArr[0].split("-", 0);
                String[] deadLineTimeArr = null;
                if (deadLineArr.length > 1) {
                    deadLineTimeArr = deadLineArr[1].split(":");
                }

                mYear = Integer.parseInt(deadLineDateArr[0]);
                mMonth = Integer.parseInt(deadLineDateArr[1]);
                mDay = Integer.parseInt(deadLineDateArr[2]);
                if (deadLineTimeArr != null) {
                    mHour = Integer.parseInt(deadLineTimeArr[0]);
                    mMinute = Integer.parseInt(deadLineTimeArr[1]);
                } else {
                    mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    mMinute = mCalendar.get(Calendar.MINUTE);
                }

            } else {
                editTime.setText("タスクの期限を設定");

                mYear = mCalendar.get(Calendar.YEAR);
                mMonth = mCalendar.get(Calendar.MONTH);
                mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                mMinute = mCalendar.get(Calendar.MINUTE);
            }

            datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;

                    timePickerDialog.show();
                }
            }, mYear, mMonth, mDay);

            timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;

                    deadLine = String.valueOf(mYear) + "-" +
                            String.format("%02d", mMonth) + "-" +
                            String.format("%02d", mDay) + " " +
                            String.format("%02d", mHour) + ":" +
                            String.format("%02d", mMinute);
                    mCalendar.set(mYear, mMonth, mDay, mHour, mMinute);
                    editTime.setText(deadLine);
                }
            }, mHour, mMinute, true);

            builder.setView(content);
            builder.setMessage("タスクを編集/削除")
                    .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            editTodo = null;
            super.onDismiss(dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // TODO: inflate a fragment view
            View rootView = super.onCreateView(inflater, container, savedInstanceState);
            ButterKnife.bind(this, rootView);
            return rootView;
        }

        @OnClick({R.id.editTask, R.id.editTime, R.id.saveBtn, R.id.deleteBtn})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.editTask:
                    break;
                case R.id.editTime:
                    datePickerDialog.show();
                    break;
                case R.id.saveBtn:
                    // タスク名がカラだったら無効
                    if (editTask.getText().toString().equals("") || editTask.getText() == null) {
                        Toast.makeText(MainActivity.this, "タスク名が入力されていません。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // saveを選ばれたときの処理
                    editTodo.setTask(editTask.getText().toString());
                    editTodo.setDeadline(deadLine);

                    saveTodo(editTodo);
                    // アラーム仕込む処理
                    setTodoAlarm(editTodo, (int) editTodo.getId(), mCalendar);
                    dismiss();
                    break;
                case R.id.deleteBtn:
                    deleteTodo(editTodo);
                    dismiss();
                    break;
            }
        }
    }

    // TODO 別ファイル化
    // 入力用ダイアログ
    public class InputDialogFragment extends DialogFragment {
        @BindView(R.id.dialogEditText)
        EditText dialogEditText;
        @BindView(R.id.dialogBtn)
        Button dialogBtn;
        @BindView(R.id.edit_date)
        TextView editDate;
        @BindView(R.id.dateResetBtn)
        ImageView dateResetBtn;
        @BindView(R.id.edit_time)
        TextView editTime;
        private DatePickerDialog mDlgDatePicker;
        private TimePickerDialog mDlgTimePicker;
        private Calendar mCalendar;
        private GregorianCalendar mMaxDate = new GregorianCalendar();
        private GregorianCalendar mMinDate = new GregorianCalendar();
        private DatePicker mDatePicker;
        private TimePicker mTimePicker;
        private int mYear;
        private int mMonth;
        private int mDay;
        private int mHour;
        private int mMinute;
        private boolean mSetTime;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dialog_input, null);
            ButterKnife.bind(this, content);

            dialogEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // キーボードを隠す処理
                    if (hasFocus == false) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            });

            // datepicker用の初期情報取得
            mCalendar = Calendar.getInstance();
            mYear = mCalendar.get(Calendar.YEAR);
            mMonth = mCalendar.get(Calendar.MONTH);
            mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
            mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
            mMinute = mCalendar.get(Calendar.MINUTE);
            mSetTime = false;

            mDlgDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    editDate.setText(String.valueOf(year) + "年" + String.format("%1$02d", monthOfYear + 1) + "月" + String.format("%1$02d", dayOfMonth) + "日");
                    mCalendar.set(year, monthOfYear, dayOfMonth);

                    mDlgTimePicker.show();
                }
            }, mYear, mMonth, mDay);

            mDlgTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    editTime.setText(String.format("%1$02d", hourOfDay) + "時" + String.format("%1$02d", minute) + "分");
                    mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                    mSetTime = true;
                }
            }, mHour, mMinute, true);

            // TODO もう少しおしゃれに
            mMaxDate.set(2020, 11, 31);
            mMinDate.set(2016, 0, 1);

            builder.setView(content);
            builder.setMessage("タスクを登録")
                    .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            return builder.create();
        }


        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @OnClick({R.id.dialogEditText, R.id.dialogBtn, R.id.edit_date, R.id.dateResetBtn, R.id.edit_time})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.dialogEditText:
                    break;
                case R.id.dialogBtn:
                    Todo todo = new Todo();
                    // テキストを取得
                    String sEditText = String.valueOf(dialogEditText.getText());

                    // テキストが空なら終わり
                    if (sEditText.equals("")) {
                        Toast.makeText(getActivity(), "タスク名を登録してください。", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    todo.setTask(sEditText);

                    // 過去の日付が登録されていたらダメ
                    Calendar nowCalendar = Calendar.getInstance();

                    int diff = nowCalendar.compareTo(mCalendar);

                    if (diff > 0 && mSetTime) {
                        Toast.makeText(getActivity(), "過去の日付は登録できません", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 入力エリアを初期化
                    dialogEditText.setText("");

                    // 日付が指定されていた場合、登録
                    if (mSetTime) {
                        // 日付を取得
                        String deadline = String.valueOf(mCalendar.get(Calendar.YEAR)) + "-" +
                                String.format("%02d", mCalendar.get(Calendar.MONTH) + 1) + "-" +
                                String.format("%02d", mCalendar.get(Calendar.DAY_OF_MONTH)) + " " +
                                String.format("%02d", mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                                String.format("%02d", mCalendar.get(Calendar.MINUTE));
                        todo.setDeadline(deadline);
                    }

                    // DBに追加
                    Time time = new Time("Asia/Tokyo");
                    time.setToNow();

                    TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
                    SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

                    ContentValues newTask = new ContentValues();
                    newTask.put(TodoContract.Todos.COL_TASK, todo.getTask());
                    newTask.put(TodoContract.Todos.COL_DEADLINE, todo.getDeadline());
                    newTask.put(TodoContract.Todos.COL_IS_DONE, 0);
                    newTask.put(TodoContract.Todos.COL_CREATED_AT, time.year + "-" + (time.month + 1) + "-" + time.monthDay);

                    long newId = db.insert(TodoContract.Todos.TABLE_NAME, null, newTask);
                    todo.setId(newId);
                    mList.add(0, todo);
                    mAdapter.notifyDataSetChanged();

                    db.close();

                    if (mSetTime) {
                        // アラームの登録
                        setTodoAlarm(todo, (int) newId, mCalendar);
                        mSetTime = false;
                    }

                    dismiss();
                    break;
                case R.id.edit_date:
                    mDatePicker = mDlgDatePicker.getDatePicker();
                    if (mDatePicker != null) {
                        mDatePicker.setMaxDate(mMaxDate.getTimeInMillis());
                        mDatePicker.setMinDate(mMinDate.getTimeInMillis());
                    }

                    // datePickerを表示
                    mDlgDatePicker.show();
                    break;
                case R.id.dateResetBtn:
                    mCalendar = Calendar.getInstance();
                    editDate.setText("日付登録にはこちらをクリック");
                    editTime.setText("");

                    Log.e("@@@@@x", "x button");
                    mSetTime = false;
                    break;
                case R.id.edit_time:
                    mDlgTimePicker.show();
                    break;
            }
        }
    }
}

