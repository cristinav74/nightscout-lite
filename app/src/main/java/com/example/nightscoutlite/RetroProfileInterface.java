package com.example.nightscoutlite;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

@JsonInclude(JsonInclude.Include.NON_NULL)
interface RetroProfileInterface {

    @GET("profile.json")
    Call<List<NightscoutProfile>> loadData();

}

