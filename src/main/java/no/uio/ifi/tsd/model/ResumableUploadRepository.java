package no.uio.ifi.tsd.model;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumableUploadRepository extends CrudRepository<ResumableUpload, String> {

	Optional<ResumableUpload> findById(String id);

	void delete(ResumableUpload resumableUpload);

	ResumableUpload save(ResumableUpload resumableUpload);

	Iterable<ResumableUpload> findAll();
	
	
}
