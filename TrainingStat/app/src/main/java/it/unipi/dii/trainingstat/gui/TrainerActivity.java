package it.unipi.dii.trainingstat.gui;

import androidx.appcompat.app.AppCompatActivity;

import it.unipi.dii.trainingstat.DatabaseManager;
import it.unipi.dii.trainingstat.R;
import it.unipi.dii.trainingstat.TrainingSession;
import it.unipi.dii.trainingstat.entities.User;
import it.unipi.dii.trainingstat.entities.UserSession;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

public class TrainerActivity extends AppCompatActivity implements View.OnClickListener {
    private User user;
    private TrainingSession trainingSession;
    private DatabaseManager databaseManager;
    private int numPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("User");
        trainingSession = (TrainingSession) intent.getSerializableExtra("TrainingSession");
        TextView usernameTV = findViewById(R.id.trainerUsernameTV);
        TextView sessionIdTV = findViewById(R.id.trainerSessionIdTV);
        usernameTV.setText(user.getUsername());
        sessionIdTV.setText(trainingSession.getId());
        Button startStopButton = findViewById(R.id.trainerStartStopButton);
        startStopButton.setText(R.string.trainerStartButton);
        databaseManager = new DatabaseManager();
        databaseManager.listenUserSessions(trainingSession.getId(), this::addUserButton);
        numPlayers = 0;
    }

    public void startStopButtonClicked(View view) {
        Button button = (Button) view;
        if (button.getText().toString().equals(this.getResources().getString(R.string.trainerStartButton))) {
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            button.setText(R.string.trainerStopButton);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String startDate = df.format(calendar.getTime());
            trainingSession.setStartDate(startDate);
            databaseManager.updateTrainingStartDate(trainingSession.getId(), startDate);
        }
        else if(button.getText().toString().equals(this.getResources().getString(R.string.trainerStopButton))) {
            button.setText(R.string.trainerStoppedButton);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String endDate = df.format(calendar.getTime());
            trainingSession.setEndDate(endDate);
            databaseManager.updateTrainingEndDate(trainingSession.getId(), endDate);
            Function<UserSession, Void> addUserSession = this::addUserSession;
            databaseManager.listenUserSessions(trainingSession.getId(), this::addUserSession);
            databaseManager.setEndedStatus(trainingSession.getId());
            trainingSession.setEndedStatus();
        }
    }

    public Void addUserButton(UserSession userSession) {
        Button button = new Button(this);
        button.setText(userSession.getUsername());
        button.setOnClickListener(this);
        LinearLayout linearLayout = findViewById(R.id.trainerUsersLinearLayout);
        linearLayout.addView(button);
        numPlayers++;
        return null;
    }

    public Void addUserSession(UserSession userSession) {
        trainingSession.addUserSession(userSession);
        if (trainingSession.getUserSessions().keySet().size() == numPlayers) {
            databaseManager.removeUserSessionsListener(trainingSession.getId());
            user.addPastSession(trainingSession.getId(), trainingSession.getStartDate());
            databaseManager.addUserPastSessions(user.getUsername(), user.getPastSessions());
            UserSession aggregateResults = computeAggregateresults();
            databaseManager.writeUserSession(trainingSession.getId(), aggregateResults);
        }
        return null;
    }

    private UserSession computeAggregateresults() {
        UserSession aggregateResults = new UserSession();
        aggregateResults.setUsername(user.getUsername());
        // Calcolo
        return aggregateResults;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        Log.d("Test", button.getText().toString());
    }
}