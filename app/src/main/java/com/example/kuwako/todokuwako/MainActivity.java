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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private TodoListAdapter mAdapter;
    private DialogFragment mNewFragment;
    private ArrayList<Todo> mList;
    private String logTag = "@@@@@BAITALK_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNewFragment = new InputDialogFragment();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewFragment.show(getFragmentManager(), "aaa");
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

        // TODO 削除。必ず初期化
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

        // TODO 削除
        Log.v(logTag, "Count: " + c.getCount());

        // TODO 削除。DB内部確認
        checkDB();

        c.close();
        db.close();

        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(logTag, "触ったよ");
            }
        });

        // シングルタップでモーダル表示 + 編集削除
        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO editFragmentの呼び出し

            }
        });

        // 長押しでタスク削除
        todoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(logTag, "ロングタッチ " + String.valueOf(position) + " " + String.valueOf(id));

                Todo todo = (Todo) parent.getItemAtPosition(position);
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

    public class EditDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // TODO ダイアログ内の動き
            // TODO ダイアログのviewセット
            // TODO 選択されたTaskの取得
            // TODO editを選ばれたときの処理
            // TODO deleteを選ばれたときの処理
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
            View content = inflater.inflate(R.layout.dailog_input, null);

            mEditDate = (TextView) content.findViewById(R.id.edit_date);
            mEditTime = (TextView) content.findViewById(R.id.edit_time);
            mDlgButton = (Button) content.findViewById(R.id.dailogBtn);
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

                        // アラームの登録
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
                        intent.putExtra("task", todo.getTask());
                        intent.putExtra("deadline", todo.getDeadline());
                        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pending);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pending);
                        }

                        mSetTime = false;
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

                    // Dialogを閉じる
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

