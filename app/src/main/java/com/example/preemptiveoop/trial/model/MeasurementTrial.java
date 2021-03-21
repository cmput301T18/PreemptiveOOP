package com.example.preemptiveoop.trial.model;

import android.location.Location;

import java.util.Date;

public class MeasurementTrial extends GenericTrial {
    Double result;

    public MeasurementTrial() {}
    public MeasurementTrial(String creator, Date creationDate, Location location, Double result) {
        super(creator, creationDate, location, result.toString());
        this.result = result;
    }

    public Double getResult() { return result; }

    public void setResult(Double result) {
        super.setResultStr(result.toString());
        this.result = result;
    }
}
