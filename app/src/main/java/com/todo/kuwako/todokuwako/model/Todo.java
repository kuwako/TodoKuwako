package com.todo.kuwako.todokuwako.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kuwako on 2016/02/16.
 */
public class Todo implements Parcelable {
    long id;
    String task; // タスク名
    int is_done; // 完了フラグ
    String deadline; // 期限
    int is_snooze; // スヌーズするか
    int snooze_interval; // スヌーズのインターバル
    String label; // ラベル
    int level; // レベル
    int status; // ステータス
    String created_at; // 登録日時
    String completed_at; // 完了日時

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getIs_done() {
        return is_done;
    }

    public void setIs_done(int is_done) {
        this.is_done = is_done;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getIs_snooze() {
        return is_snooze;
    }

    public void setIs_snooze(int is_snooze) {
        this.is_snooze = is_snooze;
    }

    public int getSnooze_interval() {
        return snooze_interval;
    }

    public void setSnooze_interval(int snooze_interval) {
        this.snooze_interval = snooze_interval;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCompleted_at() {
        return completed_at;
    }

    public void setCompleted_at(String completed_at) {
        this.completed_at = completed_at;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.task);
        dest.writeInt(this.is_done);
        dest.writeString(this.deadline);
        dest.writeInt(this.is_snooze);
        dest.writeInt(this.snooze_interval);
        dest.writeString(this.label);
        dest.writeInt(this.level);
        dest.writeInt(this.status);
        dest.writeString(this.created_at);
        dest.writeString(this.completed_at);
    }

    public Todo() {
    }

    protected Todo(Parcel in) {
        this.id = in.readLong();
        this.task = in.readString();
        this.is_done = in.readInt();
        this.deadline = in.readString();
        this.is_snooze = in.readInt();
        this.snooze_interval = in.readInt();
        this.label = in.readString();
        this.level = in.readInt();
        this.status = in.readInt();
        this.created_at = in.readString();
        this.completed_at = in.readString();
    }

    public static final Parcelable.Creator<Todo> CREATOR = new Parcelable.Creator<Todo>() {
        @Override
        public Todo createFromParcel(Parcel source) {
            return new Todo(source);
        }

        @Override
        public Todo[] newArray(int size) {
            return new Todo[size];
        }
    };
}
