package org.udg.pds.todoandroid.entity;

import java.util.List;

/**
 * Created by imartin on 12/02/16.
 */
public class User {
  public long id;
  public String username;
  public String email;
    public String name;
    public String description;
    // public Image user_picture; ????
    // No sé si s'ha de fer servir aquesta classe.
    public List<Integer> followers;
    public List<Integer> following;
    // public List<Publication> publications; ??
    // S'ha de crear la classe publication

}
