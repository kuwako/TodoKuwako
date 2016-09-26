package com.example.kuwako.todokuwako.listener;

/**
 * Created by kuwako on 2016/09/26.
 */
import com.example.kuwako.todokuwako.model.Todo;

import java.util.EventListener;

public interface InputDialogListener extends EventListener {
    public Todo setTodo(Todo todo);
}
