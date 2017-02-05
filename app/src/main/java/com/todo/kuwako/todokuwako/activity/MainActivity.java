package com.todo.kuwako.todokuwako.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.todo.kuwako.todokuwako.BuildConfig;
import com.todo.kuwako.todokuwako.R;
import com.todo.kuwako.todokuwako.adapter.TodoListAdapter;
import com.todo.kuwako.todokuwako.contract.TodoContract;
import com.todo.kuwako.todokuwako.fragment.EditDialogFragment;
import com.todo.kuwako.todokuwako.fragment.InputDialogFragment;
import com.todo.kuwako.todokuwako.listener.EditDialogListener;
import com.todo.kuwako.todokuwako.listener.InputDialogListener;
import com.todo.kuwako.todokuwako.model.Todo;
import com.todo.kuwako.todokuwako.receiver.AlarmBroadcastReceiver;
import com.todo.kuwako.todokuwako.sqlite.TodoOpenHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements InputDialogListener, EditDialogListener {

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
    private ArrayList<Todo> mList;
    private String logTag = "@@@@@BAITALK_TAG";
    private FirebaseAnalytics mFirebaseAnalytics;
    private Todo editTodo = null;

    // TODO EventBus導入
    // TODO Dagger2導入
    // TODO DBをrealmに移行
    // TODO 関数をHelper系クラスに移行
    // TODO Push通知送信できるようにする
    // TODO Dialogのデザイン改修
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.todo.kuwako.todokuwako.R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(BuildConfig.DEBUG);
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
        mAdapter = new TodoListAdapter(MainActivity.this) {
        };
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

                EditDialogFragment edf = EditDialogFragment.newInstance(editTodo);
                edf.setEditDialogListener(MainActivity.this);
                Bundle bundle = new Bundle();
                bundle.putParcelable("editTodo", editTodo);
                edf.setArguments(bundle);
                edf.setCancelable(false);
                edf.show(getFragmentManager(), "fff");
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // デフォルトだとAlarm側から起動されるintentは
        // Activityに留まったままAlarmManagerからAへの新しいIntentを複数回
        // 投げたところ、getIntent()で取得するIntentがActivity起動時のものから変わらない
        super.onNewIntent(intent);
        // 画面表示時に再度起動された際にgetIntent()を更新する。
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        // 通知から立ち上げた時にそのタスクのモーダルを開く
        if (intent.getIntExtra("todoId", 0) != 0) {
            for (int i = 0; mList.size() > i; i++) {
                Todo targetTodo = mList.get(i);

                if ((int) targetTodo.getId() == intent.getIntExtra("todoId", 0)) {
                    editTodo = targetTodo;
                    break;
                }
            }

            if (editTodo != null) {
                EditDialogFragment edf = EditDialogFragment.newInstance(editTodo);
                edf.setEditDialogListener(MainActivity.this);
                Bundle bundle = new Bundle();
                bundle.putParcelable("editTodo", editTodo);
                bundle.putBoolean("showSnooze", true);
                edf.setArguments(bundle);
                edf.setCancelable(false);
                edf.show(getFragmentManager(), "fff");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("parcelableTodo", editTodo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        editTodo = savedInstanceState.getParcelable("parcelableTodo");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(MainActivity.this, "setting", Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.setClass(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    public void addList(View view) {
        String task = String.valueOf(editTask.getText());
        if (task.equals("")) {
            return;
        }

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

    @OnClick({R.id.addButton, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                addList(view);
                break;
            case R.id.fab:
                InputDialogFragment idf = InputDialogFragment.newInstance();
                idf.setInputDialogListener(this);
                idf.setCancelable(false);
                idf.show(getFragmentManager(), "aaa");
                break;
        }
    }

    public void setTodo(Todo todo, Calendar calendar) {
        long newId = insertTodoForDb(todo);
        todo.setId(newId);
        mList.add(0, todo);
        mAdapter.notifyDataSetChanged();
        Toast.makeText(this, todo.getTask(), Toast.LENGTH_SHORT).show();

        if (todo.getDeadline() != null) {
            setTodoAlarm(todo, calendar);
        }
    }

    private long insertTodoForDb(Todo todo) {
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
        return newId;
    }

    public void saveTodo(Todo todo, Calendar calendar) {
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

        setTodoAlarm(todo, calendar);

        return;
    }

    public void deleteTodo(Todo todo) {
        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues updateTask = new ContentValues();
        updateTask.put(TodoContract.Todos.COL_IS_DONE, 1);

        db.update(
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

        if (todo.getDeadline() != null) {
            deleteTodoAlarm(todo);
        }
        return;
    }

    public void setTodoAlarm(Todo todo, Calendar calendar) {
        if (todo.getDeadline() == null || todo.getDeadline().equals("")) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        intent.putExtra("task", todo.getTask());
        intent.putExtra("deadline", todo.getDeadline());
        intent.putExtra("id", (int) todo.getId());
        intent.putExtra("todoId", (int) todo.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), (int) todo.getId(), intent, 0);
        checkDB();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        }
    }

    public void deleteTodoAlarm(Todo todo) {
        if (todo.getDeadline() == null || todo.getDeadline().equals("")) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), (int) todo.getId(), intent, 0);
        pending.cancel();
        alarmManager.cancel(pending);
    }
}

