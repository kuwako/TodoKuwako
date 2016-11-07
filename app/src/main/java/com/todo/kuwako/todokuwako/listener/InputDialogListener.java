package com.todo.kuwako.todokuwako.listener;

/**
 * Created by kuwako on 2016/09/26.
 */
import com.todo.kuwako.todokuwako.model.Todo;

import java.util.Calendar;
import java.util.EventListener;

public interface InputDialogListener extends EventListener {
    public void setTodo(Todo todo, Calendar calendar);
}
