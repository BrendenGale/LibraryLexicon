package edu.weber.cs.w1283820.librarylexicon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class DeleteDeckDialog extends DialogFragment {

    private String owner;
    private String name;

    public DeleteDeckDialog(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to delete " + name + "?");
        builder.setMessage("Deleted decks cannot be recovered");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Query query = FirebaseFirestore.getInstance().collection("decks")
                        .whereEqualTo("owner", owner)
                        .whereEqualTo("name", name);

                query.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        deleteCards(document.getId());
                                        DocumentReference theDocument = FirebaseFirestore.getInstance().document("decks/" + document.getId());
                                        theDocument.delete();
                                    }
                                }
                            }
                        });

                Toast.makeText(getContext(), name + " Has Been Deleted", Toast.LENGTH_SHORT).show();
                dismiss();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();

    }

    public void deleteCards(String deckID) {

        Query query = FirebaseFirestore.getInstance().collection("cards")
                .whereEqualTo("deckID", deckID);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference theDocument = FirebaseFirestore.getInstance().document("cards/" + document.getId());
                                theDocument.delete();
                            }
                        }
                    }
                });

    }

}