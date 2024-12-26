package model;

import android.os.Parcel;
import android.os.Parcelable;

public class Workout implements Parcelable{
    private int id; // 主键
    private String type; // 健身类型
    private String startTime;// 开始时间
    private int duration; // 健身时间
    private String difficulty; // 健身难度

    // Constructor
    public Workout(int id, String type, String startTime, int duration, String difficulty) {
        this.id = id;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
        this.difficulty = difficulty;
    }
    public Workout(String type, String startTime, int duration, String difficulty) {
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
        this.difficulty = difficulty;
    }

    // 读取对象的数据
    public static final Creator<Workout> CREATOR = new Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // 写入对象的数据到 Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(startTime);
        dest.writeInt(duration);
        dest.writeString(difficulty);
    }

    // 从 Parcel 读取对象的数据
    protected Workout(Parcel in) {
        id = in.readInt();
        type = in.readString();
        startTime = in.readString();
        duration = in.readInt();
        difficulty = in.readString();
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration=" + duration +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
