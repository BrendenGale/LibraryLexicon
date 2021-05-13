package edu.weber.cs.w1283820.librarylexicon;

import android.os.Bundle;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SignInFragment.signInFrag,
                                                    DeckDisplayFragment.deckDisplayFrag,
                                                    NewDeckFragment.newDeckFrag,
                                                    ViewDeckFragment.viewDeckFrag{


    private Toolbar toolbar;
    private DeckDisplayFragment deckDisplayFrag;
    private GoogleSignInClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.INVISIBLE);
        Fresco.initialize(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragContainer, new SignInFragment(), "currentFrag")
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client != null){
            FirebaseAuth.getInstance().signOut();
            client.signOut();
        }
    }

    //Interface methods --------------------------------------------------------------------

    @Override
    public void signedIn(FirebaseUser user, GoogleSignInClient client) {
        this.client = client;
        toolbar.setVisibility(View.VISIBLE);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new DeckDisplayFragment(user), "currentFrag")
                .commit();

    }

    @Override
    public void newDeck(FirebaseUser user) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new NewDeckFragment(user), "currentFrag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void newDeckMade(FirebaseUser user) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new DeckDisplayFragment(user), "currentFrag")
                .commit();
    }

    @Override
    public void deleteDialog(String owner, String name) {
        DeleteDeckDialog dialog = new DeleteDeckDialog(owner, name);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "delete_dialog");
    }

    @Override
    public void openViewDeckFragment(String name, String format, FirebaseUser user, String deckID) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new ViewDeckFragment(name, format, user, deckID), "currentFrag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openAddCard(FirebaseUser user, String deckName, String deckID, String format) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new AddCardFragment(user, deckName, deckID, format), "currentFrag")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openViewCard(String imageUrl) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new ViewCardFragment(imageUrl), "currentFrag")
                .addToBackStack(null)
                .commit();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        client.signOut();
        toolbar.setVisibility(View.INVISIBLE);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(fm.findFragmentByTag("currentFrag").getId(), new SignInFragment(), "currentFrag")

                .commit();
        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
    }
}