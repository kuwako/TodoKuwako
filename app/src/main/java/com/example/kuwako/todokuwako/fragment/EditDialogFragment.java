package com.example.kuwako.todokuwako.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kuwako.todokuwako.R;
import com.example.kuwako.todokuwako.activity.MainActivity;
import com.example.kuwako.todokuwako.model.Todo;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditDialogFragment extends DialogFragment {
    private EditDialogListener listener = null;
    @BindView(R.id.editTask)
    EditText editTask;
    @BindView(R.id.editTime)
    TextView editTime;
    @BindView(R.id.saveBtn)
    Button saveBtn;
    @BindView(R.id.deleteBtn)
    Button deleteBtn;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    Calendar mCalendar = Calendar.getInstance();
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    String deadLine;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        if (editTodo == null) {
            dismiss();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_edit, null);
        ButterKnife.bind(this, content);

        editTask.setText(editTodo.getTask());
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

        // TODO そもそも期限を削除する機能も必要そう。
        if (editTodo.getDeadline() != null) {
            editTime.setText(editTodo.getDeadline());
            String[] deadLineArr = editTodo.getDeadline().split(" ", 0);
            String[] deadLineDateArr = deadLineArr[0].split("-", 0);
            String[] deadLineTimeArr = null;
            if (deadLineArr.length > 1) {
                deadLineTimeArr = deadLineArr[1].split(":");
            }

            mYear = Integer.parseInt(deadLineDateArr[0]);
            mMonth = Integer.parseInt(deadLineDateArr[1]);
            mDay = Integer.parseInt(deadLineDateArr[2]);
            if (deadLineTimeArr != null) {
                mHour = Integer.parseInt(deadLineTimeArr[0]);
                mMinute = Integer.parseInt(deadLineTimeArr[1]);
            } else {
                mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                mMinute = mCalendar.get(Calendar.MINUTE);
            }

        } else {
            editTime.setText("タスクの期限を設定");

            mYear = mCalendar.get(Calendar.YEAR);
            mMonth = mCalendar.get(Calendar.MONTH);
            mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
            mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
            mMinute = mCalendar.get(Calendar.MINUTE);
        }

        datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;

                timePickerDialog.show();
            }
        }, mYear, mMonth, mDay);

        timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;

                deadLine = String.valueOf(mYear) + "-" +
                        String.format("%02d", mMonth) + "-" +
                        String.format("%02d", mDay) + " " +
                        String.format("%02d", mHour) + ":" +
                        String.format("%02d", mMinute);
                mCalendar.set(mYear, mMonth, mDay, mHour, mMinute);
                editTime.setText(deadLine);
            }
        }, mHour, mMinute, true);

        builder.setView(content);
        builder.setMessage("タスクを編集/削除")
                .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        editTodo = null;
        super.onDismiss(dialog);
    }

    @OnClick({R.id.editTask, R.id.editTime, R.id.saveBtn, R.id.deleteBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editTask:
                break;
            case R.id.editTime:
                datePickerDialog.show();
                break;
            case R.id.saveBtn:
                // タスク名がカラだったら無効
                if (editTask.getText().toString().equals("") || editTask.getText() == null) {
                    Toast.makeText(MainActivity.class, "タスク名が入力されていません。", Toast.LENGTH_SHORT).show();
                    return;
                }
                // saveを選ばれたときの処理
                editTodo.setTask(editTask.getText().toString());
                editTodo.setDeadline(deadLine);

                saveTodo(editTodo);
                // アラーム仕込む処理
                // TODO 再度編集した場合にアラームならない？
                setTodoAlarm(editTodo, (int) editTodo.getId(), mCalendar);
                dismiss();
                break;
            case R.id.deleteBtn:
                deleteTodo(editTodo);
                dismiss();
                break;
        }
    }

    public EditDialogFragment() {
        // Required empty public constructor
    }

    public static EditDialogFragment newInstance(Todo todo) {
        EditDialogFragment fragment = new EditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("todo", (Parcelable) todo);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}