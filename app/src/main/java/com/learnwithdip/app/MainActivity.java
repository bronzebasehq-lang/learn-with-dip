package com.learnwithdip.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {
    private Button bLearn, bQuiz, bProfile;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        if (!new Store(this).isOnboarded()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        bLearn = findViewById(R.id.nav_learn);
        bQuiz = findViewById(R.id.nav_quiz);
        bProfile = findViewById(R.id.nav_profile);
        bLearn.setOnClickListener(v -> show(new LearnFragment(), bLearn));
        bQuiz.setOnClickListener(v -> show(new QuizFragment(), bQuiz));
        bProfile.setOnClickListener(v -> show(new ProfileFragment(), bProfile));
        show(new LearnFragment(), bLearn);
    }

    private void show(Fragment f, Button active) {
        getFragmentManager().beginTransaction().replace(R.id.container, f).commit();
        for (Button b : new Button[]{bLearn, bQuiz, bProfile}) {
            b.setSelected(b == active);
            b.setTextColor(b == active ? 0xFFFFFFFF : 0xFF9CA3AF);
        }
    }

    public void goQuiz() { show(new QuizFragment(), bQuiz); }
    public void goLearn() { show(new LearnFragment(), bLearn); }

    @Override
    protected void onResume() {
        super.onResume();
        WidgetUpdater.update(this);
    }
}
