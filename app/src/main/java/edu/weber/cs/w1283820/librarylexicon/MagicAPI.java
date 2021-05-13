package edu.weber.cs.w1283820.librarylexicon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MagicAPI {

    @GET("cards")
    Call<SearchedCardList> getSearchedCardList();

    @GET("cards")
    Call<SearchedCardList> getSearchedCardList(@Query("name") String name, @Query("gameFormat") String format);

}
