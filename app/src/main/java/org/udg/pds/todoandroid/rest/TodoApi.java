package org.udg.pds.todoandroid.rest;

import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.entity.Task;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import org.udg.pds.todoandroid.entity.UserToReg;
import org.udg.pds.todoandroid.entity.PublicationPost;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by imartin on 13/02/17.
 */
public interface TodoApi {
    @POST("/users/login")
    Call<User> login(@Body UserLogin login);

    @POST("/users/register")
    Call<User> register(@Body UserToReg register);

    @POST("/users/logout")
    Call<String> logout();

    @GET("/users/check")
    Call<String> check();

    @POST("/tasks")
    Call<IdObject> addTask(@Body Task task);


    @GET("/tasks")
    Call<List<Task>> getTasks();

    @GET("/users")
    Call<List<User>> getUsers();

    @GET("/tasks/{id}")
    Call<Task> getTask(@Path("id") String id);

    // Com saber quan cridar a un /self o a un /{id} des de l'aplicació??
    @GET("/users/self")
    Call<User> getUserProfile();

    @GET("/users/{id}")
    Call<User> getUserProfileByID(@Path("id") Long id);

    @GET("/users/self/publications")
    Call<List<Publication>> getUserPublications();

    @GET("/users/{id}/publications")
    Call<List<Publication>> getUserPublicationsByID(@Path("id") Long id);

    @GET("/publications")
    Call<List<Publication>> getPublications();

    @POST("/publications")
    Call<Publication> postPublication(@Body PublicationPost p);

    @DELETE("/publications/{id}")
    Call<String> deletePublication(@Path("id") Long id);
}

