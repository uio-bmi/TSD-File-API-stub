package no.uio.ifi.tsd.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ResumableUploads {

	private List<ResumableUpload> resumables;

}
