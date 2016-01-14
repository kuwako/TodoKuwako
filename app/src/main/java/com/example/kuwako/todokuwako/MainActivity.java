package com.example.kuwako.todokuwako;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
                TodoContract.Todos.COL_CREATED_AT + " > ? and " + TodoContract.Todos.COL_IS_DONE + " < ?", // where
                new String[] { "2016-01-02", "1"}, // where arg
                null, // group by
                null, // having
                TodoContract.Todos.COL_CREATED_AT + " desc"// order by
        );

        Log.v("DB_TEST", "Count: " + c.getCount());

        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex(TodoContract.Todos._ID));
            String task = c.getString(c.getColumnIndex(TodoContract.Todos.COL_TASK));
            int is_done = c.getInt(c.getColumnIndex(TodoContract.Todos.COL_IS_DONE));
            String created_at = c.getString(c.getColumnIndex(TodoContract.Todos.COL_CREATED_AT));

            Log.v("DB_TEST", "id: " + id + " task: " + task + " is_done: " + is_done + " created_at: " + created_at);
        }

        c.close();
        db.close();

        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListView listView = (ListView)parent;
                // クリックされたアイテムを返す
                String item = (String)listView.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, item + " is completed.", Toast.LENGTH_LONG).show();

                mAdapter.remove(item);
            }

        });
    }


    public void addList(View view) {
        EditText et = (EditText)findViewById(R.id.editText);
        Editable sTodo = et.getText();

        mAdapter.add(String.valueOf(sTodo));
        Log.e("adapter", String.valueOf(sTodo));
        et.setText("");
    }
}
