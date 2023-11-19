package com.example.desicuisine.Notification;

import com.example.desicuisine.Notification.Sending.MyResponse;
import com.example.desicuisine.Notification.Sending.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA6SL5QhA:APA91bHrN1x7YgNSveHR9Vx6zTUpF_GNKiZhF8uyjwN7wHWXhaaSBxlpfaUFAu63exFRcYD9yB2D9YhkvLp38ZhZzpGOXSGgErdpzCbUWFwDzEmiEd2I9bZ5Cc1FKBZoShgYBNesWVX_"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

