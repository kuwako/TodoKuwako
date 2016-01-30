package com.example.kuwako.todokuwako;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Layout;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

        ListView todoListView = (ListView)findViewById(R.id.todoListView);
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
                new String[] { "1" }, // where arg
                null, // group by
                null, // having
                TodoContract.Todos.COL_CREATED_AT + " desc"// order by
        );

        while(c.moveToNext()) {
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

                ListView listView = (ListView)parent;
                // クリックされたアイテムを返す
                String item = (String)listView.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, item + " is completed.", Toast.LENGTH_LONG).show();

                TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
                SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

                ContentValues updateTask = new ContentValues();
                updateTask.put(TodoContract.Todos.COL_IS_DONE, 1);

                int updateCount = db.update(
                        TodoContract.Todos.TABLE_NAME,
                        updateTask,
                        TodoContract.Todos.COL_TASK + " = ?",
                        new String[] { item }
                );
                db.close();

                mAdapter.remove(item);

                checkDB();
            }

        });
    }


    public void addList(View view) {

        EditText et = (EditText)findViewById(R.id.editText);
        Editable sTodo = et.getText();

        mAdapter.add(String.valueOf(sTodo));
        Time time = new Time("Asia/Tolyo");
        time.setToNow();

        TodoOpenHelper todoOpenHelper = new TodoOpenHelper(MainActivity.this);
        SQLiteDatabase db = todoOpenHelper.getWritableDatabase();

        ContentValues newTask = new ContentValues();
        newTask.put(TodoContract.Todos.COL_TASK, String.valueOf(sTodo));
        newTask.put(TodoContract.Todos.COL_IS_DONE, 0);
        newTask.put(TodoContract.Todos.COL_CREATED_AT, time.year + "-" + (time.month + 1) + "-" + time.monthDay);

        long newId = db.insert(TodoContract.Todos.TABLE_NAME, null, newTask);

        db.close();

        Log.e("adapter", String.valueOf(sTodo));
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

    public static class InputDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(R.layout.dailog_input, null);
            builder.setView(content);

            builder.setMessage("テスト")
                    .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            return  builder.create();
        }
    }
}
