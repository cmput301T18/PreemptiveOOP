package com.example.preemptiveoop.experiment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.preemptiveoop.R;
import com.example.preemptiveoop.experiment.model.Experiment;
import com.example.preemptiveoop.experiment.model.GenericExperiment;
import com.example.preemptiveoop.user.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExperimentList extends AppCompatActivity {
    private User user;
    private boolean searchMode;

    private ArrayList<Experiment> experiments;
    private ArrayAdapter<Experiment> expAdapter;

    private EditText etKeywords;
    private Button btSearch;

    private RadioGroup rgExpType;
    private ListView expListView;
    private FloatingActionButton btAddExp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment_list);

        // get passed-in arguments
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra(".user");
        searchMode = intent.getBooleanExtra(".searchMode", false);

        etKeywords  = findViewById(R.id.EditText_keywords);
        btSearch    = findViewById(R.id.Button_search);

        rgExpType   = findViewById(R.id.RadioButton_expType);
        expListView = findViewById(R.id.ListView_experiments);
        btAddExp    = findViewById(R.id.Button_addExp);

        experiments = new ArrayList<>();
        expAdapter = new ExpArrayAdatper(this, experiments, user);
        expListView.setAdapter(expAdapter);

        expListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ManageExperiment fragment = new ManageExperiment(experiments.get(position), user);
                fragment.show(getSupportFragmentManager(), "MANAGE_EXPERIMENT");
            }
        });

        btSearch.setOnClickListener(this::btSearchOnClick);

        rgExpType.setOnCheckedChangeListener(this::rgExpTypeOnCheckedChanged);
        btAddExp.setOnClickListener(this::btAddExpOnClick);

        if (searchMode) {
            rgExpType.setVisibility(View.GONE);
            return;
        }

        etKeywords.setVisibility(View.GONE);
        btSearch.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateExperimentList();
    }

    private void updateExpListFromQueryTask(Task<QuerySnapshot> task) {
        experiments.clear();
        for (QueryDocumentSnapshot document : task.getResult()) {
            GenericExperiment exp = document.toObject(GenericExperiment.class);
            experiments.add(exp.toCorrespondingExp());
        }

        Collections.sort(experiments, new Comparator<Experiment>() {
            @Override
            public int compare(Experiment o1, Experiment o2) { return o2.getCreationDate().compareTo(o1.getCreationDate()); }
        });
        expAdapter.notifyDataSetChanged();
    }

    private void displayOwnedExpList() {
        CollectionReference expCol = FirebaseFirestore.getInstance().collection("Experiments");
        // perform query
        expCol.whereEqualTo("owner", user.getUsername())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d("ExperimentList.DB", "Failed to get owned experiments.", task.getException());
                            return;
                        }
                        updateExpListFromQueryTask(task);
                    }
                });
    }

    private void displayPartiExpList() {
        CollectionReference expCol = FirebaseFirestore.getInstance().collection("Experiments");
        // perform query
        expCol.whereArrayContains("experimenters", user.getUsername())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d("ExperimentList.DB", "Failed to get participated experiments.", task.getException());
                            return;
                        }
                        updateExpListFromQueryTask(task);
                    }
                });
    }

    private void displaySearchedExpList(String keyword) {
        CollectionReference expCol = FirebaseFirestore.getInstance().collection("Experiments");
        // perform query
        expCol.whereArrayContains("keywords", keyword).whereEqualTo("status", Experiment.STATUS_PUBLISHED)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d("ExperimentList.DB", "Failed to search for experiments.", task.getException());
                            return;
                        }
                        updateExpListFromQueryTask(task);
                    }
                });
    }

    public void updateExperimentList() {
        if (searchMode) {
            displaySearchedExpList(etKeywords.getText().toString());
            return;
        }

        int checkId = rgExpType.getCheckedRadioButtonId();
        rgExpTypeOnCheckedChanged(rgExpType, checkId);
    }

    public void btSearchOnClick(View v) {
        String keyword = etKeywords.getText().toString();
        if (keyword.isEmpty())
            return;
        displaySearchedExpList(keyword);
    }

    public void rgExpTypeOnCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.RadioButton_ownedExp:
                btAddExp.setVisibility(View.VISIBLE);
                displayOwnedExpList();
                break;
            case R.id.RadioButton_partiExp:
                btAddExp.setVisibility(View.GONE);
                displayPartiExpList();
                break;
        }
    }

    public void btAddExpOnClick(View v) {
        PublishExperiment fragment = new PublishExperiment(user);
        fragment.show(getSupportFragmentManager(), "PUBLISH_EXPERIMENT");
    }
}