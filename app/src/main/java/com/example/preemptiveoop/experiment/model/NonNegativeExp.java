package com.example.preemptiveoop.experiment.model;

import android.location.Location;

import java.util.Date;

public class NonNegativeExp extends Experiment <Trial<Integer>> {
    public NonNegativeExp() {}
    public NonNegativeExp(String databaseId, String owner, Date creationDate, String description,
                    Location region, boolean requireLocation, int requiredNumOfTrial) {
        super(databaseId, Experiment.TYPE_COUNT, owner, creationDate, description, region, requireLocation, requiredNumOfTrial);
    }

    @Override
    public void addTrial(Trial<Integer> trial) {
        if (trial.getResult() < 0)
            throw new IllegalArgumentException("Trial for NonNegativeExp can only have a result that's >= 0.");
        super.addTrial(trial);
    }
}