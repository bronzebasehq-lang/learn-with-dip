package com.learnwithdip.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Onboarding 4 bước, chỉ hiện lần đầu mở app. */
public class OnboardingActivity extends Activity {
    private LinearLayout step1, step2, step3, step4;
    private String goal = "Tiếng Anh chuyên ngành AI/Tech/Code";
    private int minutes = 10;
    private Button g1, g2, g3, m5, m10, m20;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_onboarding);

        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        EditText etName = findViewById(R.id.et_name);
        EditText etAge = findViewById(R.id.et_age);

        findViewById(R.id.btn_s1).setOnClickListener(v -> {
            if (etName.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Nhập tên của bạn nhé", Toast.LENGTH_SHORT).show();
                return;
            }
            show(step2);
        });
        findViewById(R.id.btn_s2).setOnClickListener(v -> {
            if (etAge.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Nhập tuổi của bạn nhé", Toast.LENGTH_SHORT).show();
                return;
            }
            show(step3);
        });

        g1 = findViewById(R.id.goal1); g2 = findViewById(R.id.goal2); g3 = findViewById(R.id.goal3);
        g1.setOnClickListener(v -> pickGoal(g1, "Tiếng Anh chuyên ngành AI/Tech/Code"));
        g2.setOnClickListener(v -> pickGoal(g2, "Tiếng Anh giao tiếp"));
        g3.setOnClickListener(v -> pickGoal(g3, "Từ vựng dev cơ bản"));
        pickGoal(g1, goal);
        findViewById(R.id.btn_s3).setOnClickListener(v -> show(step4));

        m5 = findViewById(R.id.min5); m10 = findViewById(R.id.min10); m20 = findViewById(R.id.min20);
        m5.setOnClickListener(v -> pickMin(m5, 5));
        m10.setOnClickListener(v -> pickMin(m10, 10));
        m20.setOnClickListener(v -> pickMin(m20, 20));
        pickMin(m10, 10);

        findViewById(R.id.btn_start).setOnClickListener(v -> {
            int age;
            try { age = Integer.parseInt(etAge.getText().toString().trim()); }
            catch (NumberFormatException e) { age = 0; }
            new Store(this).saveOnboarding(
                    etName.getText().toString().trim(), age, goal, minutes);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        show(step1);
    }

    private void show(LinearLayout step) {
        for (LinearLayout l : new LinearLayout[]{step1, step2, step3, step4})
            l.setVisibility(l == step ? View.VISIBLE : View.GONE);
    }

    private void pickGoal(Button b, String g) {
        goal = g;
        for (Button x : new Button[]{g1, g2, g3})
            x.setBackgroundResource(x == b ? R.drawable.btn_primary : R.drawable.btn_ghost);
    }

    private void pickMin(Button b, int m) {
        minutes = m;
        for (Button x : new Button[]{m5, m10, m20})
            x.setBackgroundResource(x == b ? R.drawable.btn_primary : R.drawable.btn_ghost);
    }
}
