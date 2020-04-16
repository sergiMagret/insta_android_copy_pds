package org.udg.pds.todoandroid.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.List;

/**
 * Created by imartin on 12/02/16.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id", scope = User.class)
public class User {
    public Long id;
    public String username;
    public String email;
    public String name;
    public String description;
    public String profilePicture;
    public Integer numberPublications;
    public Integer numberFollowers;
    public Integer numberFollowed;

    public String getUsername() {
        return username;
    }
    public String getName() {
        return name;
    }
}
