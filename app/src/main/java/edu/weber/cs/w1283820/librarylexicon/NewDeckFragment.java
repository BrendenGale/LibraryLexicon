package edu.weber.cs.w1283820.librarylexicon;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class NewDeckFragment extends Fragment {

    private View root;
    private FirebaseUser user;
    private EditText deckNameInput;
    private Spinner formatSpinner;
    private Button createDeckButton;
    private newDeckFrag mCallBack;

    public interface newDeckFrag {
        public void newDeckMade(FirebaseUser user);
    }

    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mCallBack = (NewDeckFragment.newDeckFrag) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "Must implement newDeckFrag");
        }

    }

    public NewDeckFragment(FirebaseUser user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_new_deck, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        deckNameInput = root.findViewById(R.id.deckViewNameInput);

        formatSpinner = root.findViewById(R.id.formatSpinner);
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<String>(getContext(),
                                                                android.R.layout.simple_list_item_1,
                                                                getResources().getStringArray
                                                                                (R.array.formats));
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);

        createDeckButton = root.findViewById(R.id.createDeckButton);
        createDeckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDeck();
            }
        });

    }

    private void submitDeck() {
        String name = deckNameInput.getText().toString();
        if(name.replaceAll("\\s", "").equals("")){
            Toast.makeText(getContext(), "Deck Name Cannot Be Blank", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = user.getEmail();

        Query query = FirebaseFirestore.getInstance().collection("decks")
                        .whereEqualTo("owner", email)
                        .whereEqualTo("name", name);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0) {
                        Toast.makeText(getContext(), "Deck's cannot have duplicate names", Toast.LENGTH_LONG).show();
                    }
                    else {
                        storeTheDeck(email, name);
                    }
                }
            }
        });

    }

    private void storeTheDeck(String email, String name) {
        String format = formatSpinner.getSelectedItem().toString();
        Map<String, Object> newDeck = new HashMap<String, Object>();
        newDeck.put("name", name);
        newDeck.put("format", format);
        newDeck.put("owner", email);
        newDeck.put("legal", "Not Legal");//decks default to non-legal, because they have no cards
        //store documents as DeckName Owner'sEmail
        DocumentReference theDocument = FirebaseFirestore.getInstance().document("decks/" + name + " " + email);
        theDocument.set(newDeck).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), name + " Has Been Saved", Toast.LENGTH_SHORT).show();
                mCallBack.newDeckMade(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error, " + name + " Was Not Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

}