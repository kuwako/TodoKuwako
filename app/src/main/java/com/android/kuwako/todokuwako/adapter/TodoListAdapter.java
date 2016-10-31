package com.android.kuwako.todokuwako.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.kuwako.todokuwako.R;
import com.android.kuwako.todokuwako.model.Todo;

import java.util.ArrayList;

/**
 * Created by kuwako on 2016/02/16.
 */
public abstract class TodoListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<Todo> todoArrayList;

    public TodoListAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTodoArrayList(ArrayList<Todo> todoArrayList) {
        this.todoArrayList = todoArrayList;
    }

    @Override
    public int getCount() {
        return todoArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return todoArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return todoArrayList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.todorow, parent, false);

        ((TextView) convertView.findViewById(R.id.task)).setText(todoArrayList.get(position).getTask());
        ((TextView) convertView.findViewById(R.id.deadline)).setText(todoArrayList.get(position).getDeadline());

        return convertView;
    }
}

