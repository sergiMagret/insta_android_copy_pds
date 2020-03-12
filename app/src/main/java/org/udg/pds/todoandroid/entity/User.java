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
    public String description;
    // public Image user_picture; ????
    // No sé si s'ha de fer servir aquesta classe.
    public List<Integer> followers;
    public List<Integer> following;
    // public List<Publication> publications; ??
    // S'ha de crear la classe publication

}
