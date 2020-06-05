package org.udg.pds.todoandroid.entity;

import android.net.Uri;
import android.widget.ImageView;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id", scope = User.class)
public class Publication {
    public Long id;
    public String photo;
    public String description;
    public Date date;
    public Long userId;
    public String userUsername;
    public List<String> hashtags;
}
