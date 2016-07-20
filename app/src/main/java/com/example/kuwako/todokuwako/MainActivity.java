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

public class MainActivity extends AppCompatActivity {

    private TodoListAdapter mAdapter;
    private DialogFragment mInputFragment;
    private DialogFragment mEditFragment;
    private ArrayList<Todo> mList;
    private String logTag = "@@@@@BAITALK_TAG";
    private FirebaseAnalytics mFirebaseAnalytics;
    private Todo editTodo = null;

    // TODO DBをrealmに移行
    // TODO 関数をService系クラスに移行
    // TODO スヌーズ機能
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mInputFragment = new InputDialogFragment();
        mEditFragment = new EditDialogFragment();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInputFragment.show(getFragmentManager(), "aaa");
            }
        });

        mList = new ArrayList<>();
        mAdapter = new TodoListAdapter(MainActivity.this) {
            @Override
            public void onClick(View v) {
                Log.d(logTag, "aaaaa触ったよ");
            }
        };
        mAdapter.setTodoArrayList(mList);

        final ListView todoListView = (ListView) findViewById(R.id.todoListView);
        todoListView.setAdapter(mAdapter);

        // db処理
        // db open
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        // 削除。必ず初期化
//        db.execSQL(TodoOpenHelper.DROP_TABLE);
//        db.execSQL(TodoOpenHelper.CREATE_TABLE);
//        db.execSQL(TodoOpenHelper.INIT_TABLE);

        // 処理
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

            mList.add(todo);
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

        // 長押しでタスク削除
        todoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(logTag, "ロングタッチ " + String.valueOf(position) + " " + String.valueOf(id));

                Todo todo = (Todo) parent.getItemAtPosition(position);
                deleteTodo(todo);
                return true;
            }
        });
    }

    public void addList(View view) {
        EditText et = (EditText) findViewById(R.id.editText);
        String task = String.valueOf(et.getText());

        Todo todo = new Todo();
        todo.setTask(task);
        mList.add(todo);

        Time time = new Time("Asia/Tokyo");
        time.setToNow();

        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues newTask = new ContentValues();
        newTask.put(TodoContract.Todos.COL_TASK, task);
        newTask.put(TodoContract.Todos.COL_IS_DONE, 0);
        newTask.put(TodoContract.Todos.COL_CREATED_AT, time.year + "-" + (time.month + 1) + "-" + time.monthDay);

        long newId = db.insert(TodoContract.Todos.TABLE_NAME, null, newTask);

        db.close();

        Log.e(logTag, task);
        et.setText("");

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

        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), alarmId, intent, 0);
        Log.e("@@@", String.valueOf(alarmId));
        checkDB();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        }
    }

    public class EditDialogFragment extends DialogFragment {
        private int mYear;
        private int mMonth;
        private int mDay;
        private int mHour;
        private int mMinute;
        Calendar mCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog;
        TimePickerDialog timePickerDialog;
        EditText editTask;
        TextView editTime;
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
            Button saveBtn = (Button) content.findViewById(R.id.saveBtn);
            Button deleteBtn = (Button) content.findViewById(R.id.deleteBtn);
            editTask = (EditText) content.findViewById(R.id.editTask);
            editTask.setText(editTodo.getTask());
            editTime = (TextView) content.findViewById(R.id.editTime);

            // TODO そもそも期限を削除する機能も必要そう。
            if (editTodo.getDeadline() != null) {
                editTime.setText(editTodo.getDeadline());
                String[] deadLineArr = editTodo.getDeadline().split(" ", 0);
                String[] deadLineDateArr = deadLineArr[0].split("-", 0);
                String[] deadLineTimeArr = deadLineArr[1].split(":");
                mYear = Integer.parseInt(deadLineDateArr[0]);
                mMonth = Integer.parseInt(deadLineDateArr[1]);
                mDay = Integer.parseInt(deadLineDateArr[2]);
                mHour = Integer.parseInt(deadLineTimeArr[0]);
                mMinute = Integer.parseInt(deadLineTimeArr[1]);

            } else {
                editTime.setText("タスクの期限を設定");

                mYear = mCalendar.get(Calendar.YEAR);
                mMonth = mCalendar.get(Calendar.MONTH);
                mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                mMinute = mCalendar.get(Calendar.MINUTE);
            }

            editTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePickerDialog.show();
                }
            });

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

            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // タスク名がカラだったら無効
                    if (editTask.getText().toString() != "") {
                        Toast.makeText(MainActivity.this, "タスク名が入力されていません。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // saveを選ばれたときの処理
                    editTodo.setTask(editTask.getText().toString());
                    editTodo.setDeadline(deadLine);

                    saveTodo(editTodo);
                    // アラーム仕込む処理
                    setTodoAlarm(editTodo, (int) editTodo.getId(), mCalendar);
                    // TODO 既にアラームが登録済みならば変更する処理
                    dismiss();
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // deleteを選ばれたときの処理
                    deleteTodo(editTodo);
                    editTodo = null;
                    dismiss();
                }
            });
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

    }

    // TODO 別ファイル化
    // 入力用ダイアログ
    public class InputDialogFragment extends DialogFragment {
        private DatePickerDialog mDlgDatePicker;
        private TimePickerDialog mDlgTimePicker;
        private Calendar mCalendar;
        private GregorianCalendar mMaxDate = new GregorianCalendar();
        private GregorianCalendar mMinDate = new GregorianCalendar();
        private DatePicker mDatePicker;
        private TimePicker mTimePicker;
        private Button mDlgButton;
        private TextView mEditDate;
        private TextView mEditTime;
        private EditText mEditText;
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

            mEditDate = (TextView) content.findViewById(R.id.edit_date);
            mEditTime = (TextView) content.findViewById(R.id.edit_time);
            mDlgButton = (Button) content.findViewById(R.id.dialogBtn);
            mEditText = (EditText) content.findViewById(R.id.dialogEditText);
            ImageView resetDateBtn = (ImageView) content.findViewById(R.id.dateResetBtn);

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
                    mEditDate.setText(String.valueOf(year) + "年" + String.format("%1$02d", monthOfYear + 1) + "月" + String.format("%1$02d", dayOfMonth) + "日");
                    mCalendar.set(year, monthOfYear, dayOfMonth);

                    mDlgTimePicker.show();
                }
            }, mYear, mMonth, mDay);

            mDlgTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mEditTime.setText(String.format("%1$02d", hourOfDay) + "時" + String.format("%1$02d", minute) + "分");
                    mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                    mSetTime = true;
                }
            }, mHour, mMinute, true);

            // TODO もう少しおしゃれに
            mMaxDate.set(2020, 11, 31);
            mMinDate.set(2016, 0, 1);

            // 日付け削除ボタン
            resetDateBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 日付初期化
                            mCalendar = Calendar.getInstance();
                            mEditDate.setText("日付登録にはこちらをクリック");
                            mEditTime.setText("");

                            Log.e("@@@@@x", "x button");
                            mSetTime = false;
                        }
                    });

            // 日付指定ボタン
            mEditDate.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDatePicker = mDlgDatePicker.getDatePicker();
                            if (mDatePicker != null) {
                                mDatePicker.setMaxDate(mMaxDate.getTimeInMillis());
                                mDatePicker.setMinDate(mMinDate.getTimeInMillis());
                            }

                            // datePickerを表示
                            mDlgDatePicker.show();
                        }
                    }
            );

            // 時間指定ボタン
            mEditTime.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // timePickerを表示
                            mDlgTimePicker.show();
                        }
                    }
            );

            mDlgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Todo todo = new Todo();
                    // テキストを取得
                    String sEditText = String.valueOf(mEditText.getText());

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
                    mEditText.setText("");

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

                    // タスクに追加
                    mList.add(todo);

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
                    db.close();

                    if (mSetTime) {
                        // アラームの登録
                        setTodoAlarm(todo, (int) newId, mCalendar);
                        mSetTime = false;
                    }

                    dismiss();
                }
            });

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
    }
}

