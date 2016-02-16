package com.example.kuwako.todokuwako;

/**
 * Created by kuwako on 2016/02/16.
 */
public class Todo {
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
}
