package org.udg.pds.todoandroid.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Created by imartin on 12/02/16.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id", scope = Comment.class)
public class Comment {
    public Long id;
    public String userUsername;
    public String text;

    /* followsUser is used to know if the logged user is following the asked user. */
    /* Let's say you are the logged user A, and you request for the profile of user B, then if you are following B,
        the value of followsUser will be true because you (A) are following B. */
    /* If the request is for the logged user profile, followsUser will be false. */

    public String getText() {
        return text;
    }
    public String getUsername() {
        return userUsername;
    }
}
