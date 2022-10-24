package com.pictManager.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pm_album")
public class Album {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @ManyToOne
    private User user;

    @Getter
    @Setter
    @ManyToMany()
    private List<Picture> pictures;

    @Getter
    @Setter
    @ManyToMany()
    private List<User> user_shared;

    @Getter
    @Setter
    private boolean isMyAlbum = true;

    @Getter
    @Setter
    @ElementCollection()
    private List<String> tags;

    public Album(){}

    public Album(User user, String name){
        this.user = user;
        this.name = name;
        this.pictures = new ArrayList<Picture>();
        this.user_shared = new ArrayList<User>();
        this.tags = new ArrayList<String>();
        this.isMyAlbum = true;
    }

    public void addPicture(Picture picture) throws Exception {
        if (this.pictures.contains(picture)) {
            throw new Exception("Pictures already in album");
        }
        this.pictures.add(picture);
    }

    public void removePicture(Picture picture) throws Exception {
        if (!this.pictures.contains(picture)) {
            throw new Exception("Pictures not in album");
        }
        this.pictures.remove(picture);
    }

    public void share(User user) throws Exception {
        if (this.user == user){
            throw new Exception("Can't share to himself");
        }
        if (this.user_shared.contains(user)) {
            throw new Exception("User can already access to this album");
        }
        this.user_shared.add(user);
    }

    public void unshare(User user) throws Exception {
        if (this.user == user){
            throw new Exception("Can't remove yourself");
        }
        if (!this.user_shared.contains(user)) {
            throw new Exception("Album not share with this user");
        }
        this.user_shared.remove(user);
    }

    public void addTag(String tag) throws Exception {
        if (this.tags.contains(tag)){
            throw new Exception("Tag already applied");
        }
        this.tags.add(tag);
    }
    
    public void deleteTag(String tag) throws Exception {
        if (!this.tags.contains(tag)){
            throw new Exception("Tag not in list");
        }
        this.tags.remove(tag);
    }
}
