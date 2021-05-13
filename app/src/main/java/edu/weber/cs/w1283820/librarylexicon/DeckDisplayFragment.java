package edu.weber.cs.w1283820.librarylexicon;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class DeckDisplayFragment extends Fragment {

    private View root;
    private FirebaseUser user;
    private Button newDeckButton;
    private RecyclerView deckRecycler;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;
    private deckDisplayFrag mCallBack;

    public interface deckDisplayFrag{
        public void newDeck(FirebaseUser user);
        public void deleteDialog(String owner, String name);
        public void openViewDeckFragment(String name, String format, FirebaseUser user, String deckID);
    }

    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mCallBack = (DeckDisplayFragment.deckDisplayFrag) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + "Must implement deckDisplayFrag");
        }

    }

    public DeckDisplayFragment(FirebaseUser user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_deck_display, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();

        newDeckButton = root.findViewById(R.id.newDeckButton);
        newDeckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.newDeck(user);
            }
        });
        firebaseFirestore = FirebaseFirestore.getInstance();
        deckRecycler = root.findViewById(R.id.deckRecycler);

        Query query = firebaseFirestore.collection("decks").whereEqualTo("owner", user.getEmail());
        FirestoreRecyclerOptions<Deck> options = new FirestoreRecyclerOptions.Builder<Deck>()
                .setQuery(query, Deck.class).build();

        adapter = new FirestoreRecyclerAdapter<Deck, DeckViewHolder>(options) {
            @NonNull
            @Override
            public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list, parent, false);
                return new DeckViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DeckViewHolder holder, int position, @NonNull Deck model) {
                holder.deckName.setText(model.getName());
                holder.format.setText(model.getFormat());

                holder.deckName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Query query = FirebaseFirestore.getInstance().collection("decks")
                                .whereEqualTo("owner", model.getOwner())
                                .whereEqualTo("name", model.getName());

                        query.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                mCallBack.openViewDeckFragment(model.getName(), model.getFormat(), user, document.getId());
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Error: Could Not Reach Firebase",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                });

                holder.deckName.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mCallBack.deleteDialog(model.getOwner(), model.getName());
                        return true;
                    }
                });

            }
        };

        deckRecycler.setHasFixedSize(false);
        deckRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        deckRecycler.setAdapter(adapter);

        adapter.startListening();
    }

    private class DeckViewHolder extends RecyclerView.ViewHolder {

        private TextView deckName;
        private TextView format;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);

            deckName = itemView.findViewById(R.id.deckNameListCaption);
            format = itemView.findViewById(R.id.deckFormatListCaption);

        }



    }

    public void updateList() {

    }

}