package com.example.kuwako.todokuwako.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.text.format.Time;

import com.example.kuwako.todokuwako.R;
import com.example.kuwako.todokuwako.activity.MainActivity;
import com.example.kuwako.todokuwako.contract.TodoContract;
import com.example.kuwako.todokuwako.model.Todo;
import com.example.kuwako.todokuwako.sqlite.TodoOpenHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputDialogFragment extends DialogFragment {
    @BindView(R.id.dialogEditText)
    EditText dialogEditText;
    @BindView(R.id.dialogBtn)
    Button dialogBtn;
    @BindView(R.id.edit_date)
    TextView editDate;
    @BindView(R.id.dateResetBtn)
    ImageView dateResetBtn;
    @BindView(R.id.edit_time)
    TextView editTime;
    private DatePickerDialog mDlgDatePicker;
    private TimePickerDialog mDlgTimePicker;
    private Calendar mCalendar;
    private GregorianCalendar mMaxDate = new GregorianCalendar();
    private GregorianCalendar mMinDate = new GregorianCalendar();
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private boolean mSetTime;

    private OnFragmentInteractionListener mListener;

    public InputDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InputDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InputDialogFragment newInstance(String param1, String param2) {
        InputDialogFragment fragment = new InputDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_input_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_input, null);
        ButterKnife.bind(this, content);

        dialogEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // キーボードを隠す処理
                if (hasFocus == false) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        // datepicker用の初期情報取得
        mCalendar = Calendar.getInstance();
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mSetTime = false;

        mDlgDatePicker = new DatePickerDialog(MainActivity.class, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editDate.setText(String.valueOf(year) + "年" + String.format("%1$02d", monthOfYear + 1) + "月" + String.format("%1$02d", dayOfMonth) + "日");
                mCalendar.set(year, monthOfYear, dayOfMonth);

                mDlgTimePicker.show();
            }
        }, mYear, mMonth, mDay);

        mDlgTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                editTime.setText(String.format("%1$02d", hourOfDay) + "時" + String.format("%1$02d", minute) + "分");
                mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                mSetTime = true;
            }
        }, mHour, mMinute, true);

        // TODO もう少しおしゃれに
        mMaxDate.set(2020, 11, 31);
        mMinDate.set(2016, 0, 1);

        builder.setView(content);
        builder.setMessage("タスクを登録")
                .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    @OnClick({R.id.dialogEditText, R.id.dialogBtn, R.id.edit_date, R.id.dateResetBtn, R.id.edit_time})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialogEditText:
                break;
            case R.id.dialogBtn:
                Todo todo = new Todo();
                // テキストを取得
                String sEditText = String.valueOf(dialogEditText.getText());

                // テキストが空なら終わり
                if (sEditText.equals("")) {
                    Toast.makeText(getActivity(), "タスク名を登録してください。", Toast.LENGTH_SHORT).show();
                    return;
                }

                todo.setTask(sEditText);

                // 過去の日付が登録されていたらダメ
                Calendar nowCalendar = Calendar.getInstance();

                int diff = nowCalendar.compareTo(mCalendar);

                if (diff > 0 && mSetTime) {
                    Toast.makeText(getActivity(), "過去の日付は登録できません", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 入力エリアを初期化
                dialogEditText.setText("");

                // 日付が指定されていた場合、登録
                if (mSetTime) {
                    // 日付を取得
                    String deadline = String.valueOf(mCalendar.get(Calendar.YEAR)) + "-" +
                            String.format("%02d", mCalendar.get(Calendar.MONTH) + 1) + "-" +
                            String.format("%02d", mCalendar.get(Calendar.DAY_OF_MONTH)) + " " +
                            String.format("%02d", mCalendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                            String.format("%02d", mCalendar.get(Calendar.MINUTE));
                    todo.setDeadline(deadline);
                }

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
                todo.setId(newId);
                mList.add(0, todo);
                mAdapter.notifyDataSetChanged();

                db.close();

                if (mSetTime) {
                    // アラームの登録
                    setTodoAlarm(todo, (int) newId, mCalendar);
                    mSetTime = false;
                }

                dismiss();
                break;
            case R.id.edit_date:
                mDatePicker = mDlgDatePicker.getDatePicker();
                if (mDatePicker != null) {
                    mDatePicker.setMaxDate(mMaxDate.getTimeInMillis());
                    mDatePicker.setMinDate(mMinDate.getTimeInMillis());
                }

                // datePickerを表示
                mDlgDatePicker.show();
                break;
            case R.id.dateResetBtn:
                mCalendar = Calendar.getInstance();
                editDate.setText("日付登録にはこちらをクリック");
                editTime.setText("");

                Log.e("@@@@@x", "x button");
                mSetTime = false;
                break;
            case R.id.edit_time:
                mDlgTimePicker.show();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
