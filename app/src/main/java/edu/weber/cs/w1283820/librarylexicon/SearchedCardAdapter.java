package edu.weber.cs.w1283820.librarylexicon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchedCardAdapter extends RecyclerView.Adapter<SearchedCardAdapter.ViewHolder>{

    private ArrayList<SearchedCard> cardList;
    private searchedCardAdapter mCallBack;

    public interface searchedCardAdapter {
        public void clicked(View view, int position);
        public void viewCard(int position);
    }

    public SearchedCardAdapter(ArrayList<SearchedCard> cardList, searchedCardAdapter mCallBack) {
        this.cardList = cardList;
        this.mCallBack = mCallBack;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView cardName;
        private TextView setName;
        private ImageButton plusOneButton;
        private ImageButton eyeButton;

        public ViewHolder(final View view) {
            super(view);
            cardName = view.findViewById(R.id.searchedCardNameCaption);
            setName = view.findViewById(R.id.searchedCardNameSetCaption);
            eyeButton = view.findViewById(R.id.eyeButton);
            eyeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallBack.viewCard(getAdapterPosition());
                }
            });
            plusOneButton = view.findViewById(R.id.imageButton);
            plusOneButton.setOnClickListener(this);
            //view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mCallBack.clicked(view, getAdapterPosition());
        }


    }

    @NonNull
    @Override
    public SearchedCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searched_card_layout, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchedCardAdapter.ViewHolder holder, int position) {
        holder.cardName.setText(cardList.get(position).getName());
        holder.setName.setText(cardList.get(position).getSetName());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}
