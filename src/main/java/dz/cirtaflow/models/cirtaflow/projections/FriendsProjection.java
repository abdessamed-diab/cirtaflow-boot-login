package dz.cirtaflow.models.cirtaflow.projections;

import dz.cirtaflow.models.cirtaflow.Friend;
import dz.cirtaflow.models.cirtaflow.Profile;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "withFriends", types = {Friend.class})
public interface FriendsProjection {

    String getStatus();
    Profile getFriendProfile();


}
