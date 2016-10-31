package com.android.kuwako.todokuwako.listener;

import com.android.kuwako.todokuwako.model.Todo;

import java.util.Calendar;
import java.util.EventListener;

/**
 * Created by m_kuwako on 2016/10/03.
 */

public interface EditDialogListener extends EventListener {
    public void saveTodo(Todo todo, Calendar calendar);
    public void deleteTodo(Todo todo);
}
