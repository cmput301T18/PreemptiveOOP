package com.example.preemptiveoop.trial.model;

import android.location.Location;

import java.util.Date;

public class CountTrial extends GenericTrial {
    Integer result;

    public CountTrial() {}
    public CountTrial(String creator, Date creationDate, Location location, Integer result) {
        super(creator, creationDate, location, result.toString());
        this.result = result;
    }

    public Integer getResult() { return result; }

    public void setResult(Integer result) {
        super.setResultStr(result.toString());
        this.result = result;
    }
}
