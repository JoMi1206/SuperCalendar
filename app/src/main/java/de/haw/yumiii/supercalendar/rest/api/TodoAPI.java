package de.haw.yumiii.supercalendar.rest.api;

import java.util.List;

import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Yumiii on 22.05.16.
 */
public interface TodoAPI {

    //@GET("/todos")
    //Call<List<TodoItem>> loadTodos();

    @GET("/todos")
    Call<List<TodoItem>> getTodos();

    @GET("/todos/{date}")
    Call<List<TodoItem>> getTodosByDate(@Path("date") String date);

    @POST("/todos")
    Call<TodoItem> postTodo(@Body TodoItem item);

    @PUT("/todos/{id}")
    Call<TodoItem> putTodo(@Path("id") String id, @Body TodoItem item);

}
