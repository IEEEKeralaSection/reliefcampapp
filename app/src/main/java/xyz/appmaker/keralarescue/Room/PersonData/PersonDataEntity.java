package xyz.appmaker.keralarescue.Room.PersonData;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "person_data")
public class PersonDataEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "camp_id")
    public String camped_at;

    @ColumnInfo(name = "age")
    public String age;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "district")
    public String district;

    @ColumnInfo(name = "mobile")
    @SerializedName("phone")
    public String mobile;

    @ColumnInfo(name = "note")
    @SerializedName("notes")
    public String note;

    @ColumnInfo(name = "status")
    public String syncStatus;

    @Ignore
    @SerializedName("status")
    @ColumnInfo(name="person_status")
    public String personStatus = "new";

    public PersonDataEntity() {
    }

    public PersonDataEntity(String name, String campName, String age, String gender, String address, String district, String mobile, String note, String personStatus,String syncStatus) {
        this.name = name;
        this.camped_at = campName;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.district = district;
        this.mobile = mobile;
        this.note = note;
        this.personStatus = personStatus;
        this.syncStatus = syncStatus;
    }


}
