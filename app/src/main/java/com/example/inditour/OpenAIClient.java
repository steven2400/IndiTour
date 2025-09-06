package com.example.inditour;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIClient {
    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";

    // 🔒 Move this key to Firebase Functions or a backend API instead of hardcoding
    private static final String API_KEY = "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";

    private final OkHttpClient client = new OkHttpClient();

    public void generateItinerary(String destination, int days, String interests, ItineraryCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "You are a travel planner. Create detailed day-by-day itineraries.");
            messages.put(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", "Plan a " + days + "-day trip to " + destination +
                    " focusing on " + interests + ". Include hotels, attractions, and food.");
            messages.put(user);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            // 🔹 Async call (does not block UI)
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(response.body().string());
                            String content = responseJson
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            callback.onSuccess(content);
                        } catch (Exception e) {
                            callback.onFailure("Parsing error: " + e.getMessage());
                        }
                    } else {
                        callback.onFailure("API error: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("Error: " + e.getMessage());
        }
    }

    // 🔹 Callback interface for async result
    public interface ItineraryCallback {
        void onSuccess(String itinerary);
        void onFailure(String error);
    }
}
