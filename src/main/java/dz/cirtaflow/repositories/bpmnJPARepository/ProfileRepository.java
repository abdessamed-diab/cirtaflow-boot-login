package dz.cirtaflow.repositories.bpmnJPARepository;

import dz.cirtaflow.models.cirtaflow.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "profile", path = "profile")
public interface ProfileRepository extends CrudRepository<Profile, String>, ProfileHelper{

}
