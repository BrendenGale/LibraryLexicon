package edu.weber.cs.w1283820.librarylexicon;

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
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class AddCardFragment extends Fragment {

    private View root;
    private TextView deckNameCaption;
    private SearchView searchView;
    private Button searchGoButton;
    private RecyclerView searchedCardRecyclerView;
    private SearchedCardAdapter.searchedCardAdapter searchedCardAdapter;
    private SimpleDraweeView draweeView;
    private ImageButton cancelButton;

    private FirebaseUser user;
    private String deckName;
    private String deckID;
    private String format;

    public AddCardFragment(FirebaseUser user, String deckName, String deckID, String format) {
        this.user = user;
        this.deckName = deckName;
        this.deckID = deckID;
        this.format = format;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_add_card, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        deckNameCaption = root.findViewById(R.id.addCardDeckNameCaption);
        String temp = getString(R.string.addCardToString) + " " + deckName;
        deckNameCaption.setText(temp);

        searchView = root.findViewById(R.id.searchView);
        searchGoButton = root.findViewById(R.id.searchGoButton);
        searchGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = searchView.getQuery().toString();
                if(!temp.replaceAll("\\s", "").equals("")) {
                    apiSearch(temp);
                }
            }
        });
        searchedCardRecyclerView = root.findViewById(R.id.searchedCardRecyclerView);

        draweeView = (SimpleDraweeView) root.findViewById(R.id.searchedCardCardDisplay);
        cancelButton = root.findViewById(R.id.cancelButton);
        draweeView.setVisibility(INVISIBLE);
        cancelButton.setVisibility(INVISIBLE);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undisplayCard();
            }
        });

    }

    private void apiSearch(String name) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.magicthegathering.io/v1/")
                .addConverterFactory(GsonConverterFactory.create()).build();

        MagicAPI magicAPI = retrofit.create(MagicAPI.class);

        Call<SearchedCardList> call = magicAPI.getSearchedCardList(name, format);
        call.enqueue(new Callback<SearchedCardList>() {
            @Override
            public void onResponse(Call<SearchedCardList> call, Response<SearchedCardList> response) {
                if(!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Could Not Reach Magic The Gathering api"
                            , Toast.LENGTH_SHORT).show();
                    return;
                }

                SearchedCardList cards = response.body();

                searchedCardAdapter = new SearchedCardAdapter.searchedCardAdapter() {
                    @Override
                    public void clicked(View view, int position) {
                        SearchedCard card = cards.getCards().get(position);
                        addCard(card.getName(), String.valueOf(card.getCmc()), card.getType(),
                                String.valueOf(card.getMultiverseid()), card.getImageUrl(),
                                card.getSetName(), card.getColors());
                    }

                    @Override
                    public void viewCard(int position) {
                        displayCard(cards.getCards().get(position).getImageUrl());
                    }
                };

                SearchedCardAdapter adapter = new SearchedCardAdapter((ArrayList<SearchedCard>) cards.getCards(), searchedCardAdapter);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
                searchedCardRecyclerView.setLayoutManager(layoutManager);
                searchedCardRecyclerView.setAdapter(adapter);
                searchedCardRecyclerView.setHasFixedSize(false);

            }

            @Override
            public void onFailure(Call<SearchedCardList> call, Throwable t) {
                Toast.makeText(getContext(), "Magic The Gathering API Encountered An Error"
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCard(String cardName, String cmc, String type,
                         String multiverseid, String imageUrl, String setName, String[] colors) {

        //temporary strings, fixes issue where .put() inserts nulls into the map instead of values
        String temp = imageUrl;
        String temp2 = multiverseid;

        Map<String, Object> newCard = new HashMap<String, Object>();
        newCard.put("cardName", cardName);
        newCard.put("cmc", cmc);
        newCard.put("deckID", deckID);
        newCard.put("setName", setName);
        newCard.put("type", type);
        newCard.put("imageUrl", temp);
        newCard.put("multiverseID", temp2);
        String allColors = "";
        if(colors != null) {
            for (String color : colors) {
                allColors += color;
            }
        }
        newCard.put("colors", allColors);

        Query query = FirebaseFirestore.getInstance().collection("cards")
                .whereEqualTo("deckID", deckID)
                .whereEqualTo("multiverseID", temp2);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (task.getResult().getDocuments().size() > 0) {//just add a copy
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference theDocument = FirebaseFirestore.getInstance()
                                    .document("cards/" + document.getId());
                            String copies = document.get("copies").toString();
                            int put = 1 + Integer.parseInt(copies);
                            newCard.put("copies", String.valueOf(put));
                        }
                    }
                    else {
                        newCard.put("copies", "1");
                    }

                    //store documents as multiverseID Deck'sID
                    DocumentReference theDocument = FirebaseFirestore.getInstance().document("cards/" + multiverseid + " " + deckID);
                    theDocument.set(newCard).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), cardName + " Was Added", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error, " + cardName + " Could Not Be Added", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });


    }

    private void displayCard(String imageUrl){

        cancelButton.setVisibility(VISIBLE);
        draweeView.setVisibility(VISIBLE);
        deckNameCaption.setVisibility(INVISIBLE);
        searchView.setVisibility(INVISIBLE);
        searchGoButton.setVisibility(INVISIBLE);
        searchedCardRecyclerView.setVisibility(INVISIBLE);

        draweeView.setImageURI(imageUrl);

    }

    private void undisplayCard() {
        cancelButton.setVisibility(INVISIBLE);
        draweeView.setVisibility(INVISIBLE);
        deckNameCaption.setVisibility(VISIBLE);
        searchView.setVisibility(VISIBLE);
        searchGoButton.setVisibility(VISIBLE);
        searchedCardRecyclerView.setVisibility(VISIBLE);
    }

}