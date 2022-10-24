package com.pictManager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.pictManager.fichier.Fichier;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pm_picture")
public class Picture {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Getter
    @Setter
    @ManyToOne
    private User user;

    @Getter
    @Setter
    @OneToOne
    private Fichier file;

    @Getter
    @Setter
    @Column(name = "creation_date")
    private LocalDateTime creation_date;

    @Getter
    @Setter
    @Column(name = "update_date")
    private LocalDateTime update_date;

    @Getter
    @Setter
    @ManyToMany()
    private List<User> user_shared;

    @Getter
    @Setter
    @ElementCollection()
    private List<String> tags;

    @Getter
    @Setter
    @Column(name = "myPicture")
    private boolean myPicture;

    public Picture(){}

    public Picture(User user, Fichier file){
        this.user = user;
        this.file = file;
        this.creation_date = LocalDateTime.now();;
        this.update_date = LocalDateTime.now();
        this.user_shared = new ArrayList<User>();
        this.tags = new ArrayList<String>();
        this.myPicture = true;
    }

    public void share(User user) throws Exception {
        if (this.user == user){
            throw new Exception("Can't share to himself");
        }
        if (this.user_shared.contains(user)) {
            throw new Exception("User can already access to this picture");
        }
        this.user_shared.add(user);
    }

    public void unshare(User user) throws Exception {
        if (this.user == user){
            throw new Exception("Can't remove yourself");
        }
        if (!this.user_shared.contains(user)) {
            throw new Exception("Picture not share with this user");
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
