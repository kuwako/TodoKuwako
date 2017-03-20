package com.todo.kuwako.todokuwako.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.todo.kuwako.todokuwako.model.Todo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by kuwako on 2017/03/13.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Test
    public void addList() throws Exception {
        MainActivity activity = activityTestRule.launchActivity(null);
//        MainActivity activity = new MainActivity();
//        activity.editTask.setText("");
//        activity.onCreate(new Bundle());
//        int mListNum = activity.getmList().size();
//        activity.addList();
//
//        assertThat(0, is(mListNum));
    }

    @Test
    public void setTodo() throws Exception {

    }

    @Test
    public void saveTodo() throws Exception {

    }

    @Test
    public void deleteTodo() throws Exception {

    }

    @Test
    public void setTodoAlarm() throws Exception {

    }

    @Test
    public void deleteTodoAlarm() throws Exception {

    }

}