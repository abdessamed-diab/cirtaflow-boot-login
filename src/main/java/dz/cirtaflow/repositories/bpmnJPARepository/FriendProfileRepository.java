package dz.cirtaflow.repositories.bpmnJPARepository;

import dz.cirtaflow.models.cirtaflow.Friend;
import dz.cirtaflow.models.cirtaflow.Profile;
import dz.cirtaflow.models.cirtaflow.projections.FriendsProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "friend", path = "friend" , excerptProjection = FriendsProjection.class)
public interface FriendProfileRepository extends CrudRepository<Friend, Long> {

    Iterable<Friend> findByFriendProfile(Profile profile);
    Iterable<Friend> findByProfile(Profile profile);

    Optional<Friend> findByProfileAndFriendProfile(Profile friend, Profile FriendProfile);

}
