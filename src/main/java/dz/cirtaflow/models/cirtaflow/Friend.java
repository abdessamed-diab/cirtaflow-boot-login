package dz.cirtaflow.models.cirtaflow;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "FRIEND")
@Table(name = "CF_FRIEND")
public class Friend implements Serializable{
    private Long id;
    private String status;
    private Profile friendProfile;
    private Profile profile;


    public Friend() {
    }

    public Friend(Profile profile, Profile friendProfile) {
        this.profile = profile;
        this.friendProfile = friendProfile;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", unique = true, nullable = false, insertable = true, updatable = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(updatable = true, insertable = true, nullable = true, unique = false, name = "STATUS")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Profile.class, optional = true, cascade = CascadeType.MERGE)
    @JoinColumn(name = "PROFILE", unique = false, nullable = false, insertable = true, updatable = true,
            referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_PROFILE_FRIENDS"))
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "FRIEND_PROFILE", foreignKey = @ForeignKey(name = "FK_FRIEND_PROFILE"), updatable = true, insertable = true, unique = false, nullable = false)
    public Profile getFriendProfile() {
        return friendProfile;
    }

    public void setFriendProfile(Profile friendProfile) {
        this.friendProfile = friendProfile;
    }
}
