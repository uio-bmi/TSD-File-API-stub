package no.uio.ifi.tsd.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.tsd.enums.TokenType;
import no.uio.ifi.tsd.exception.UnauthorizedException;
import no.uio.ifi.tsd.model.Chunk;
import no.uio.ifi.tsd.model.ResumableUpload;
import no.uio.ifi.tsd.model.ResumableUploads;
import no.uio.ifi.tsd.model.User;

@RestController
@RequestMapping("/v1/{project}")
@Slf4j
@Api(value = "TSD File Api Stub")
public class TSDStubController {

	private Gson gson = new Gson();
	private static final String TSD_S_DATA_DURABLE_FILE_IMPORT = "./tsd/%s/data/durable/file-import/";
	public static final String STREAM_PROCESSING_FAILED = "stream processing failed";
	public static final String DATA_STREAMED = "data streamed";

	@ApiResponses(value = { @ApiResponse(code = 200, message = "token retrieved succesfully"),
			@ApiResponse(code = 401, message = "You are not authorized to get token"), })
	@RequestMapping(value = "/auth/tsd/token", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getToken(
			@ApiParam(value = "project ID ", required = true, example = "p11") @PathVariable String project,
			@RequestParam TokenType type,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@RequestBody User data) throws IOException {

		String userName = data.getUser_name();
		String otp = data.getOtp();
		String password = data.getPassword();

		if (userName == null || userName.isEmpty() || otp == null || otp.isEmpty() || password == null
				|| password.isEmpty() || authorization == null || authorization.isEmpty()
				|| !authorization.startsWith("Bearer")) {
			throw new UnauthorizedException();
		} else {
			return new JSONObject().put("token",
					"eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE2MTU4OSwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRQYS5zYTBpQm96MVQzVUVxWksualF1NEYzcEZMaHovci5vWXBTZTcvMFMvSkhFeHJ2cFFTUyIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkdWRhSzBpbFpOS0R5dkQ5RzNtQTdCdWJtUE1rek4xWlF4UG4ubS9vNlVscVA4ZkdLMnBPcm0iLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.bf4I1EQz812SmVa8twH6gF-BNE2QeAK-N1234567890")
					.toString();
		}
	}

	@ApiResponses(value = { @ApiResponse(code = 200, message = "token retrieved succesfully"),
			@ApiResponse(code = 401, message = "You are not authorized to get token"), })
	@RequestMapping(value = "/auth/basic/token", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getToken(
			@ApiParam(value = "project ID ", required = true, example = "p11") @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@RequestBody String data) throws IOException {
		Map<String, String> tokenMap = new ObjectMapper().readValue(data, new TypeReference<Map<String, String>>() {
		});
		String type = tokenMap.get("type");
		if (type == null || type.isEmpty() || authorization == null || authorization.isEmpty()
				|| !authorization.startsWith("Bearer")) {
			throw new UnauthorizedException();
		} else {
			return new JSONObject().put("token",
					"eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE2MTU4OSwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRQYS5zYTBpQm96MVQzVUVxWksualF1NEYzcEZMaHovci5vWXBTZTcvMFMvSkhFeHJ2cFFTUyIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkdWRhSzBpbFpOS0R5dkQ5RzNtQTdCdWJtUE1rek4xWlF4UG4ubS9vNlVscVA4ZkdLMnBPcm0iLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.bf4I1EQz812SmVa8twH6gF-BNE2QeAK-N1234567890")
					.toString();
		}
	}

	@RequestMapping(value = "/files/stream", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<?> upload(
			@ApiParam(value = "project ID ", required = true, example = "p11") @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @RequestHeader(required = false) String filename,
			InputStream fileStream) throws IOException {
		log.info("upload");

		if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer")) {
			throw new UnauthorizedException();
		} else if (filename == null || filename.isEmpty() || filename.isBlank()) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}

		Path path = Paths.get(Files.createTempDirectory("temp").toString(), filename);
		try {
			Files.copy(fileStream, path, StandardCopyOption.REPLACE_EXISTING);
			log.info(path.getParent().toString());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(createJsonMessage(DATA_STREAMED));
	}

	@RequestMapping(value = "/files/resumables", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody()
	public ResponseEntity<?> getResumableUploads(
			@ApiParam(value = "project ID ", required = true, example = "p11") @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization)
			throws IOException {
		log.info("upload");

		if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer")) {
			throw new UnauthorizedException();
		}
		ResumableUploads resumableChunks = readResumableChunks(project);
		return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(resumableChunks));
	}

	@RequestMapping(value = "/files/stream/{filename}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<?> initializeResumableUpload(
			@ApiParam(value = "project ID ", required = true, example = "p11") @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @PathVariable String filename,
			@ApiParam(value = "chunk", example = "1") @RequestParam String chunk,
			@RequestParam(value = "id", required = false) String id, @RequestBody byte[] content) throws IOException {
		log.info("upload chunk");
		if (authorization == null || authorization.isEmpty() || !authorization.startsWith("Bearer")) {
			throw new UnauthorizedException();
		} else if (filename == null || filename.isEmpty() || filename.isBlank()) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}
		if (id == null) {
			id = generateUploadID();
		}
		File uploadFolder = generateUploadFolder(String.format(TSD_S_DATA_DURABLE_FILE_IMPORT, project), id);
		ResumableUploads resumableChunks = readResumableChunks(project);

		Chunk newChunk = createChunk(filename, chunk, id);
		ResumableUpload resumableUpload = createReumableUpload(newChunk);

		if (chunk.equals("1")) {
			log.info("initializing chunk");
			File chunkFile = saveChunk(uploadFolder, filename, content);
			resumableUpload = updateResumableUpload(resumableUpload, chunkFile);
			resumableChunks.getResumables().add(resumableUpload);
		} else if ("end".equalsIgnoreCase(chunk)) {
			log.info("finalizing chunk");
			validateUploadId();
			finalizeChunks(uploadFolder, filename);
		} else {
			log.info("Upload chunks");
			validateUploadId();
			resumableUpload = getResumableUpload(id, resumableChunks);
			resumableUpload.setMaxChunk(new BigInteger(chunk));
			File chunkFile = saveChunk(uploadFolder, chunk, resumableUpload.getFileName(), content);
			resumableUpload = updateResumableUpload(resumableUpload, chunkFile);
			log.info(resumableChunks.toString());
			resumableChunks = updateResumableChunks(resumableChunks, resumableUpload, newChunk);
			log.info(resumableChunks.toString());
		}

		log.info("write Resumable");
		try (FileWriter writer = new FileWriter(getResumablesPAth(project))) {
			log.info(resumableChunks.toString());
			gson.toJson(resumableChunks, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info(gson.toJson(newChunk));
		return ResponseEntity.status(HttpStatus.CREATED).body(gson.toJson(newChunk));
	}

	private ResumableUpload updateResumableUpload(ResumableUpload resumableUpload, File chunkFile) throws IOException {
		long length = chunkFile.length();
		if (resumableUpload.getMaxChunk().equals(BigInteger.ONE)) {
			resumableUpload.setPreviousOffset(BigInteger.ZERO);
			resumableUpload.setNextOffset(BigInteger.valueOf(length));
		} else {
			resumableUpload.setPreviousOffset(resumableUpload.getNextOffset());
			resumableUpload.setNextOffset(resumableUpload.getNextOffset().add(BigInteger.valueOf(length)));
		}
		resumableUpload.setChunkSize(BigInteger.valueOf(length));
		resumableUpload.setMd5Sum(getFileChecksum(chunkFile));
		return resumableUpload;
	}

	private ResumableUploads updateResumableChunks(ResumableUploads resumableChunks, ResumableUpload resumableUpload,
			Chunk newChunk) {
		resumableUpload.setMaxChunk(new BigInteger(newChunk.getMaxChunk()));
		List<ResumableUpload> newItems = resumableChunks.getResumables().stream()
				.map(o -> o.getId() == resumableUpload.getId() ? resumableUpload : o).collect(Collectors.toList());
		resumableChunks.setResumables(newItems);
		return resumableChunks;
	}

	private Chunk createChunk(String filename, String chunk, String id) {
		Chunk newChunk = new Chunk();
		newChunk.setFileName(filename);
		newChunk.setId(id);
		newChunk.setMaxChunk(chunk);
		return newChunk;
	}

	private ResumableUpload createReumableUpload(Chunk newChunk) {
		ResumableUpload resumableUpload = new ResumableUpload();
		resumableUpload.setFileName(newChunk.getFileName());
		resumableUpload.setId(newChunk.getId());
		resumableUpload.setPreviousOffset(BigInteger.ZERO);
		resumableUpload.setMaxChunk(new BigInteger(newChunk.getMaxChunk()));
		return resumableUpload;
	}

	private ResumableUpload getResumableUpload(String id, ResumableUploads resumableChunks) {
		return resumableChunks.getResumables().stream().filter(u -> u.getId().equals(id)).findAny().get();
	}

	private String getResumablesPAth(String project) {
		return String.format(TSD_S_DATA_DURABLE_FILE_IMPORT + "/resumables.json", project);
	}

	private ResumableUploads readResumableChunks(String project) {
		ResumableUploads resumables = new ResumableUploads();
		File resumablesPAth = new File(getResumablesPAth(project));
		if (resumablesPAth.exists()) {
			log.info("read resumable");
			try (Reader reader = new FileReader(resumablesPAth)) {

				resumables = gson.fromJson(reader, ResumableUploads.class);

				log.info((resumables.toString()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			log.info("create new resumable");
			resumables.setResumables(new ArrayList<ResumableUpload>());
		}
		return resumables;
	}

	private void validateUploadId() {
		// TODO Auto-generated method stub

	}

	private File saveChunk(File uploadFolder, String chunk, String filename, byte[] content) {
		File chunkFile = new File(uploadFolder, String.format(filename + ".chunk.%s", chunk));

		try (FileWriter f = new FileWriter(chunkFile, true);
				BufferedWriter b = new BufferedWriter(f);
				PrintWriter p = new PrintWriter(b);) {

			p.println(content);

		} catch (IOException i) {
			i.printStackTrace();
		}
		log.info(chunkFile.getAbsolutePath());
		return chunkFile;
	}

	private void finalizeChunks(File uploadFolder, String id) {
		// TODO Auto-generated method stub

	}

	private File saveChunk(File uploadFolder, String filename, byte[] content) {
		return saveChunk(uploadFolder, "1", filename, content);
	}

	private File generateUploadFolder(String path, String uploadId) {
		File uploadDir = new File(path, uploadId);
		if (!uploadDir.exists()) {
			log.info(uploadDir + " not exist");
			if (uploadDir.mkdirs()) {
				log.info(uploadDir + " created");
			}
		}
		return uploadDir;
	}

	private String generateUploadID() {
		return String.valueOf(new Random(new Date().getTime()).hashCode());
	}

	private String createJsonMessage(String message) throws JSONException {
		return new JSONObject().put("message", message).toString();
	}

	private static String getFileChecksum(File file) throws IOException {
		MessageDigest digest;
		StringBuilder sb = new StringBuilder();
		try {
			digest = MessageDigest.getInstance("MD5");

			// Get file input stream for reading the file content
			FileInputStream fis = new FileInputStream(file);

			// Create byte array to read data in chunks
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;

			// Read file data and update in message digest
			while ((bytesCount = fis.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}
			;

			// close the stream; We don't need it now.
			fis.close();

			// Get the hash's bytes
			byte[] bytes = digest.digest();

			// This bytes[] has bytes in decimal format;
			// Convert it to hexadecimal format
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return complete hash
		return sb.toString();
	}
}
