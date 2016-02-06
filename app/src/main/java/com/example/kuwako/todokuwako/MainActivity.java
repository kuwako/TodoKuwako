package com.example.kuwako.todokuwako;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> mAdapter;
    private DialogFragment mNewFragment;

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

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListView todoListView = (ListView) findViewById(R.id.todoListView);
        todoListView.setAdapter(mAdapter);

        // db処理
        // db open
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

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
            mAdapter.add(
                    c.getString(c.getColumnIndex(TodoContract.Todos.COL_TASK))
            );
        }

        // TODO 削除
        Log.v("DB_TEST", "Count: " + c.getCount());

        // DB内部確認
        checkDB();

        c.close();
        db.close();

        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListView listView = (ListView) parent;
                // クリックされたアイテムを返す
                String item = (String) listView.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, item + " is completed.", Toast.LENGTH_LONG).show();

                TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
                SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

                ContentValues updateTask = new ContentValues();
                updateTask.put(TodoContract.Todos.COL_IS_DONE, 1);

                int updateCount = db.update(
                        TodoContract.Todos.TABLE_NAME,
                        updateTask,
                        TodoContract.Todos.COL_TASK + " = ?",
                        new String[]{item}
                );
                db.close();

                mAdapter.remove(item);

                checkDB();
            }

        });

    }


    public void addList(View view) {

        EditText et = (EditText) findViewById(R.id.editText);
        String sTodo = String.valueOf(et.getText());

        mAdapter.add(sTodo);
        Time time = new Time("Asia/Tokyo");
        time.setToNow();

        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues newTask = new ContentValues();
        newTask.put(TodoContract.Todos.COL_TASK, sTodo);
        newTask.put(TodoContract.Todos.COL_IS_DONE, 0);
        newTask.put(TodoContract.Todos.COL_CREATED_AT, time.year + "-" + (time.month + 1) + "-" + time.monthDay);

        long newId = db.insert(TodoContract.Todos.TABLE_NAME, null, newTask);

        db.close();

        Log.e("adapter", sTodo);
        et.setText("");

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

            Log.v("DB_TEST", "id: " + id + " task: " + task + " is_done: " + is_done + " created_at: " + created_at);
        }

        c.close();
        db.close();
    }

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

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dailog_input, null);

            mEditDate = (TextView) content.findViewById(R.id.edit_date);
            mEditTime = (TextView) content.findViewById(R.id.edit_time);
            mDlgButton = (Button) content.findViewById(R.id.dailogBtn);
            mEditText = (EditText) content.findViewById(R.id.dialogEditText);

            // datepicker用の初期情報取得
            mCalendar = Calendar.getInstance();
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mCalendar.get(Calendar.MINUTE);

            mDlgDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mEditDate.setText("日付: " + String.valueOf(year) + "年" + String.valueOf(monthOfYear + 1) + "月" + String.valueOf(dayOfMonth) + "日");
                    mDlgTimePicker.show();
                }
            }, year, month, day);

            mDlgTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mEditTime.setText("日時: " + String.valueOf(hourOfDay) + "時" + String.valueOf(minute));
                }
            }, hour, minute, true);

            mMaxDate.set(2020, 11, 31);
            mMinDate.set(2016, 0, 1);

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
                    // テキストを取得
                    String sEditText = String.valueOf(mEditText.getText());
                    mAdapter.add(sEditText);
                    // 入力エリアを初期化
                    mEditText.setText("");

                    // TODO 日付を取得

                    // DBに追加

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

