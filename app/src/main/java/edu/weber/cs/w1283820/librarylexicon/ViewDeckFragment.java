package edu.weber.cs.w1283820.librarylexicon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ViewDeckFragment extends Fragment {

    private View root;
    private Button editDeckButton;
    private Button saveButton;
    private EditText nameInput;
    private Spinner formatSpinner;
    private TextView legalityCaption;
    private TextView cardCountCaption;
    private RecyclerView cardListRecycler;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore firebaseFirestore;
    private Button addCardButton;
    private TextView averageCMC;
    private TextView colorCaption;
    private viewDeckFrag mCallBack;

    private String name;
    private String format;
    private FirebaseUser user;
    private String deckID;

    public interface viewDeckFrag {
        public void openAddCard(FirebaseUser user, String deckName, String deckID, String format);
        public void openViewCard(String imageUrl);
    }

    public ViewDeckFragment(String name, String format, FirebaseUser user, String deckID) {
        this.name = name;
        this.format = format;
        this.user = user;
        this.deckID = deckID;
    }

    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mCallBack = (ViewDeckFragment.viewDeckFrag) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "Must implement viewDeckFrag");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_view_deck, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();

        nameInput = root.findViewById(R.id.deckViewNameInput);
        nameInput.setText(name);
        nameInput.setEnabled(false);

        formatSpinner = root.findViewById(R.id.deckViewSpinner);
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray
                        (R.array.formats));
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);
        switch(format){
            case "Standard":
                formatSpinner.setSelection(0);
                break;
            case "Modern":
                formatSpinner.setSelection(1);
                break;
            case "Commander":
                formatSpinner.setSelection(2);
                break;
        }
        formatSpinner.setEnabled(false);

        averageCMC = root.findViewById(R.id.cmcCaption);
        colorCaption = root.findViewById(R.id.colorsCaption);

        editDeckButton = root.findViewById(R.id.editDeckButton);
        editDeckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();
            }
        });

        saveButton = root.findViewById(R.id.saveChangesButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
        saveButton.setVisibility(INVISIBLE);

        cardCountCaption = root.findViewById(R.id.cardCountCaption);
        legalityCaption = root.findViewById(R.id.legalityCaption);
        updateCardCount(deckID);

        cardListRecycler = root.findViewById(R.id.deckListRecyclerView);
        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("cards")
                .whereEqualTo("deckID", deckID);
        FirestoreRecyclerOptions<Card> options = new FirestoreRecyclerOptions.Builder<Card>()
                .setQuery(query, Card.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Card, CardViewHolder>(options) {
            @NonNull
            @Override
            public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardlist, parent, false);
                return new CardViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CardViewHolder holder, int position, @NonNull Card model) {
                holder.cardNameCaption.setText(model.getCardName());
                holder.cardCopiesCaption.setText(model.getCopies() + "x");
                holder.setNameCaption.setText(model.getSetName());

                holder.cardNameCaption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallBack.openViewCard(model.getImageUrl());
                    }
                });

                holder.cardNameCaption.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        deleteCard(deckID, model.getCardName());
                        return true;
                    }
                });

            }
        };

        cardListRecycler.setHasFixedSize(false);
        cardListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        cardListRecycler.setAdapter(adapter);
        adapter.startListening();

        addCardButton = root.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.openAddCard(user, name, deckID, format);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private class CardViewHolder extends RecyclerView.ViewHolder{

        private TextView cardNameCaption;
        private TextView cardCopiesCaption;
        private TextView setNameCaption;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNameCaption = itemView.findViewById(R.id.cardNameCaption);
            cardCopiesCaption = itemView.findViewById(R.id.cardCopiesCaption);
            setNameCaption = itemView.findViewById(R.id.setNameCaption);
        }



    }

    private void enableEditing(){
        nameInput.setEnabled(true);
        //formatSpinner.setEnabled(true);
        editDeckButton.setVisibility(INVISIBLE);
        saveButton.setVisibility(VISIBLE);
    }

    private void saveChanges(){

        String email = user.getEmail();

        Query query = FirebaseFirestore.getInstance().collection("decks")
                .whereEqualTo("owner", email)
                .whereEqualTo("name", name);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference theDocument = FirebaseFirestore.getInstance()
                                        .document("decks/" + document.getId());
                                Map<String, Object> changes = new HashMap<String, Object>();
                                changes.put("name", nameInput.getText().toString());
                                changes.put("format", formatSpinner.getSelectedItem());
                                changes.put("owner", email);
                                changes.put("legal", "Not Legal");
                                theDocument.set(changes).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), nameInput.getText()
                                                + "Has Been Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(),
                                                "Error: Changes Could Not Be Saved",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), "Error: Could Not Reach Firebase",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        nameInput.setEnabled(false);
        //formatSpinner.setEnabled(false);
        editDeckButton.setVisibility(VISIBLE);
        saveButton.setVisibility(INVISIBLE);

        updateCardCount(deckID);

    }

    private void deleteCard(String deckID, String cardName) {

        Query query = FirebaseFirestore.getInstance().collection("cards")
                .whereEqualTo("deckID", deckID)
                .whereEqualTo("cardName", cardName);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference theDocument = FirebaseFirestore.getInstance().document("cards/" + document.getId());
                                theDocument.delete();
                                updateCardCount(deckID);
                            }
                        }
                    }
                });

    }

    private void updateCardCount(String deckID) {
        Query query = FirebaseFirestore.getInstance().collection("cards")
                .whereEqualTo("deckID", deckID);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean legal = true;
                            String formatTemp = formatSpinner.getSelectedItem().toString();
                            int formatLimit;
                            switch(formatTemp){
                                case "Commander":
                                    formatLimit = 100;
                                    break;
                                default:
                                    formatLimit = 60;
                                    break;
                            }
                            int count = 0;
                            int totalCMC = 0;
                            String colors = "";
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int copies = Integer.parseInt(document.get("copies").toString());
                                String cardName = document.get("cardName").toString();
                                switch (cardName){
                                    case "Mountain":
                                    case "Island":
                                    case "Swamp":
                                    case "Plains":
                                    case "Forest":
                                        break;
                                    default:
                                        if(copies > 4){
                                            legal = false;
                                        }else if(copies > 1 && formatTemp.equals("Commander")) {
                                            legal = false;
                                        }
                                        break;
                                }
                                count += copies;
                                totalCMC += Integer.parseInt(document.get("cmc").toString()) * copies;
                                String cardColors = document.get("colors").toString();
                                if(!colors.contains("White") && cardColors.contains("White")) {
                                    colors += "White ";
                                }
                                if(!colors.contains("Blue") && cardColors.contains("Blue")) {
                                    colors += "Blue ";
                                }
                                if(!colors.contains("Black") && cardColors.contains("Black")) {
                                    colors += "Black ";
                                }
                                if(!colors.contains("Red") && cardColors.contains("Red")) {
                                    colors += "Red ";
                                }
                                if(!colors.contains("Green") && cardColors.contains("Green")) {
                                    colors += "Green ";
                                }

                            }

                            if(count < formatLimit) {
                                legal = false;
                                Log.d("Test", "had fewer cards");
                            }

                            cardCountCaption.setText(count + "/" + formatLimit);

                            if(count != 0) {
                                averageCMC.setText("Average CMC: " + totalCMC/count);
                                if(colors.equals("")) {
                                    colorCaption.setText("Colors: Colorless");
                                }
                                else {
                                    colorCaption.setText("Colors: " + colors);
                                }
                            }
                            else {
                                averageCMC.setText("");
                                colorCaption.setText("");
                            }
                            updateLegality(legal);

                        }
                    }
                });

    }


    private void updateLegality(boolean legal) {

        if(legal){
            legalityCaption.setText("Legal");
            legalityCaption.setTextColor(GREEN);
            return;
        }

        legalityCaption.setText("Not Legal");
        legalityCaption.setTextColor(RED);

    }

}