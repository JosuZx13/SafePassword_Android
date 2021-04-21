package pdm.safepassword.init;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityExit extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se cierra aquellas instancias que pudiesen haber quedado abiertas
        finish();
    }
}
