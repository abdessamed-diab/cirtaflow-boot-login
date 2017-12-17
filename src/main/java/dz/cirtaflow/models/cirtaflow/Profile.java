package dz.cirtaflow.models.cirtaflow;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

@Entity(name = "PROFILE")
@Table(name = "CF_PROFILE")
public class Profile implements Serializable{
    private String id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private Locale locale;
    private byte[] profilePicture;
    private List<Friend> friendList;
    private Friend friendProfile;

    public Profile() {
    }

    public Profile(String id, String name, String firstName, String lastName, String gender, Locale locale) {
        this.id= id;
        this.name= name;
        this.firstName= firstName;
        this.lastName= lastName;
        this.gender= gender;
        this.locale= locale;
    }

    @Id
    @Column(name = "ID", updatable = true, insertable = true, nullable = false, unique = true)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(unique = false, nullable = false, insertable = true, updatable = true, name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "FIRST_NAME", updatable = true, insertable = true, nullable = false, unique = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(unique = false, nullable = false, insertable = true, updatable = true, name = "LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "GENDER", updatable = true, insertable = true, nullable = false, unique = false)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Column(unique = false, nullable = false, insertable = true, updatable = true, name = "LOCALE")
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }


    @Lob
    @Column(name = "PROFILE_PICTURE", length = Integer.MAX_VALUE, updatable = true, insertable = true, nullable = true, unique = false)
    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "profile", targetEntity = Friend.class, orphanRemoval = false)
    public List<Friend> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<Friend> friendList) {
        this.friendList = friendList;
    }

    @OneToOne(mappedBy = "friendProfile", fetch = FetchType.LAZY)
    public Friend getFriendProfile() {
        return friendProfile;
    }

    public void setFriendProfile(Friend friendProfile) {
        this.friendProfile = friendProfile;
    }
}
