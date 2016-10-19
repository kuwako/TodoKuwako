package com.example.kuwako.todokuwako.listener;

import com.example.kuwako.todokuwako.model.Todo;

import java.util.Calendar;
import java.util.EventListener;

/**
 * Created by m_kuwako on 2016/10/03.
 */

public interface EditDialogListener extends EventListener {
    public void saveTodo(Todo todo);
    public void deleteTodo(Todo todo);
    public void setAlarm(Todo todo, Calendar calendar);
}
