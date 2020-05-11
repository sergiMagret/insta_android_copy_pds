package org.udg.pds.todoandroid.rest;

import org.udg.pds.todoandroid.entity.Comment;
import org.udg.pds.todoandroid.entity.CommentPost;
import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.ModifiedData;
import org.udg.pds.todoandroid.entity.Publication;
import org.udg.pds.todoandroid.entity.PublicationPost;
import org.udg.pds.todoandroid.entity.Task;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import org.udg.pds.todoandroid.entity.UserToReg;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    Call<List<User>> getUsers(@Query("text") String text, @Query("page") Integer page, @Query("size") Integer size);

    @GET("/publications/{id}/comments")
    Call<List<Comment>> getComments(@Path("id") Long publicationId, @Query("page") Integer page, @Query("size") Integer size);

    @GET("/tasks/{id}")
    Call<Task> getTask(@Path("id") String id);

    @GET("/users/self")
    Call<User> getUserProfile();

    @GET("/users/{id}")
    Call<User> getUserProfileByID(@Path("id") Long id);

    @POST("/users/self/followed")
    Call<String> addFollowed(@Body IdObject id);

    @DELETE("/users/self/followed/{id}")
    Call<String> deleteFollowed(@Path("id") Long user);

    @GET("/users/self/followed")
    Call<List<User>> getFollowed(@Query("page") Integer page, @Query("size") Integer size);

    @GET("/users/self/followers")
    Call<List<User>> getFollowers(@Query("page") Integer page, @Query("size") Integer size);

    @GET("/users/{id}/followed")
    Call<List<User>> getFollowedById(@Path("id") Long id,@Query("page") Integer page, @Query("size") Integer size);

    @GET("/users/{id}/followers")
    Call<List<User>> getFollowersById(@Path("id") Long id,@Query("page") Integer page, @Query("size") Integer size);

    @GET("/users/self/publications")
    Call<List<Publication>> getUserPublications(@Query("page") Integer page, @Query("size") Integer size);

    @GET("/users/{id}/publications")
    Call<List<Publication>> getUserPublicationsByID(@Path("id") Long id,@Query("page") Integer page, @Query("size") Integer size);

    @GET("/publications")
    Call<List<Publication>> getPublications(@Query("page") Integer page, @Query("size") Integer size);

    @POST("/publications")
    Call<String> postPublication(@Body PublicationPost p);

    @GET("/publications/{id}/likes")
    Call <List<Integer>> getLikes(@Path("id") Long id);

    @GET("/publications/{id}/nComments")
    Call <Integer> getNumComments(@Path("id") Long id);


    @POST("/publications/{id}/like")
    Call <Publication> addLike(@Path("id") Long id);

    @POST("/publications/{id}/comments")
    Call <String> sendComment(@Body CommentPost c);

    @DELETE("/publications/{id}/delLike")
    Call <Publication> deleteLike(@Path("id") Long id);

    @DELETE("/publications/{id}")
    Call<String> deletePublication(@Path("id") Long id);

    @DELETE("/publications/{publicationId}/delComment/{commentId}")
    Call<String> deleteComment(@Path("publicationId") Long publicationId, @Path("commentId") Long commentId);

    @PUT("/users/self")
    Call<String> modifyProfile(@Body ModifiedData data);


    @PUT("/publications/{publicationId}/editComment/{commentId}")
    Call<Comment> editComment(@Path("publicationId") Long publicationId, @Path("commentId") Long commentId, @Body CommentPost cp);
}

