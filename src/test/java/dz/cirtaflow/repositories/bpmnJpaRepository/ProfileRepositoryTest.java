package dz.cirtaflow.repositories.bpmnJpaRepository;

import dz.cirtaflow.context.CirtaflowBootApplicationEntryPoint;
import dz.cirtaflow.models.cirtaflow.Friend;
import dz.cirtaflow.models.cirtaflow.Profile;
import dz.cirtaflow.repositories.bpmnJPARepository.FriendProfileRepository;
import dz.cirtaflow.repositories.bpmnJPARepository.ProfileRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CirtaflowBootApplicationEntryPoint.class)
@TestPropertySource(locations = "classpath:/config/application-dev.properties")
public class ProfileRepositoryTest implements Serializable{
    private static final Logger LOG= LogManager.getLogger(ProfileRepositoryTest.class);

    Profile profile;
    List<Friend> friendsList;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private FriendProfileRepository friendProfileRepository;

    public ProfileRepositoryTest() {
    }

    @BeforeEach
    public void setup() {
        profile= new Profile("Abdou.Diab_12345678", "Abdou.Diab", "abdessamed", "diab",
                "male", Locale.FRENCH);

        this.friendsList= new ArrayList<>();
        this.friendsList.add(new Friend(profile ,
                new Profile("mehdi_genifi_878945", "mehdi_geunifi", "mehdi", "guenifi", "male", Locale.FRENCH)   )
        );
        this.profile.setFriendList(friendsList);
        this.profile = this.profileRepository.save(profile);
        this.profileRepository.save(profile);
    }

    @AfterEach
    public void tearDown() {
        this.friendProfileRepository.deleteAll();
        this.profileRepository.deleteAll();
    }

    @Test
    public void testSave() {
        this.profile.getFriendList().forEach(friend -> {
            this.friendProfileRepository.save(friend);
        });
        this.profile  = this.profileRepository.save(this.profile);
        assertTrue(this.profileRepository.count() > 0, "save failed.");
        assertEquals(1 , this.profile.getFriendList().size(), "result mismatch.");
    }

    @Test
    public void testAddFriendToFriendList() {

        Friend friend= new Friend(this.profile,
                new Profile("rokia_diab_8945645", "rokia.diab", "rokia", "diab", "female", Locale.FRENCH)
        );

        this.profile.getFriendList().add(friend);
        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });
        this.profile = this.profileRepository.save(this.profile);
        assertEquals(2, this.profile.getFriendList().size(), "result mismatch");
    }

    @Test
    public void testDeleteFriendFromFriendList() {
        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });
        assumeTrue(profile != null, "assumption fails.");
        Friend removedFriend = profile.getFriendList().remove(0);
        this.friendProfileRepository.findByFriendProfile(removedFriend.getFriendProfile()).forEach(friend -> {
            this.friendProfileRepository.delete(friend);
        });
        assertTrue(!this.friendProfileRepository.findById(removedFriend.getId()).isPresent(), "result mismatch.");
    }

    @Test
    public void testUpdateFriendOfFriendList() {
        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });

        assumeTrue(!this.profile.getFriendList().isEmpty(), "assumption fails.");
        this.friendProfileRepository.findByProfile(this.profile).forEach(friend -> {
            friend.setStatus("some NEW STATUS");
            this.friendProfileRepository.save(friend);
            assertEquals("some NEW STATUS", this.friendProfileRepository.findById(friend.getId()).get().getStatus(), "result mismatch." );
        });

    }

    @Test
    public void testSaveAlreadyExistingFriend() {
        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });

        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });

        assertEquals(this.profile.getFriendList().size(), this.friendProfileRepository.count());
    }

    @Test
    public void testSaveAndUpdateAlreadyExistingFriend() {
        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });

        this.profile.getFriendList().forEach(fr -> {
            this.friendProfileRepository.save(fr);
        });

        this.profile.getFriendList().forEach(friend -> {
            friend.setStatus("new STATUS");
            this.friendProfileRepository.save(friend);
        });

        this.profileRepository.findById(profile.getId()).get().getFriendList().forEach(friend -> {
            assertEquals("new STATUS", friend.getStatus(), "result mismatch.");
        });
    }

}
