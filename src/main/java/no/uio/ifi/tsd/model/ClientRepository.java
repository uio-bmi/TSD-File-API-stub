package no.uio.ifi.tsd.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends CrudRepository<Client, String> {

	Optional<Client> findById(String id);

	void delete(Client client);

	Client save(Client client);

	Iterable<Client> findAll();

	Client findByUserNameAndPassword(String userName, String password);

	Client findByClientIdAndPassword(String clientId, String password);
	
	
}
