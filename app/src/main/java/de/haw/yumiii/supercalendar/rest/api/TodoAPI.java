package de.haw.yumiii.supercalendar.rest.api;

import java.util.List;

import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Yumiii on 22.05.16.
 */
public interface TodoAPI {

    //@GET("/todos")
    //Call<List<TodoItem>> loadTodos();

    @GET("/todos")
    Call<List<TodoItem>> receiveTodos();

    @POST("/todos")
    Call<TodoItem> postTodo(@Body TodoItem item);

}
