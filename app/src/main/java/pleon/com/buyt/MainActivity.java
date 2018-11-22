package pleon.com.buyt;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    // TODO: implement the app with Flutter
    // to have both the top and bottom app bars create the activity with top app bar and add
    // a fragment that includes the bottom app bar in it in this activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomAppBar bottomAppBar = findViewById(R.id.bottomBar);
        setSupportActionBar(bottomAppBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_home, menu);
        return true;
    }
}
