package no.uio.ifi.tsd.model;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Entity
@Table(name = "UPLOADS")
public class ResumableUpload {
//	@Id
//	@GeneratedValue(strategy=GenerationType.SEQUENCE)
//    @Column(name = "Id", nullable = false)
//    @SerializedName("id")
//    private Long id;

	
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
	        name = "UUID",
	        strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "ID", updatable = false, nullable = false)
	@ColumnDefault("random_uuid()")
//	@Type(type = "uuid-char")
	private String id;
	
    @SerializedName("filename")
    private String fileName;

    @SerializedName("group")
    private String memberGroup;

    @SerializedName("chunk_size")
    private BigInteger chunkSize;

    @SerializedName("previous_offset")
    private BigInteger previousOffset;

    @SerializedName("next_offset")
    private BigInteger nextOffset;

    @SerializedName("max_chunk")
    private BigInteger maxChunk;

    @SerializedName("md5sum")
    private String md5Sum;


}
