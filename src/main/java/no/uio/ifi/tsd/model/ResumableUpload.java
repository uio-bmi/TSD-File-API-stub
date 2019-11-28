package no.uio.ifi.tsd.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;

import java.math.BigInteger;

@Data
@ToString
public class ResumableUpload {

    @SerializedName("id")
    private String id;

    @SerializedName("filename")
    private String fileName;

    @SerializedName("group")
    private String group;

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
