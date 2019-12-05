package no.uio.ifi.tsd.controller;

import static java.util.function.Predicate.not;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

	public static final String PROJECT = "p11";
	private Gson gson = new Gson();
	@Value("${tsd.file.import}")
	public String durableFileImport;
	public static final String STREAM_PROCESSING_FAILED = "stream processing failed";
	public static final String DATA_STREAMED = "data streamed";

	@ApiResponses(value = { @ApiResponse(code = 200, message = "token retrieved succesfully"),
			@ApiResponse(code = 401, message = "You are not authorized to get token"), })
	@PostMapping(value = "/auth/tsd/token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getToken(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@RequestParam TokenType type,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@RequestBody User data) {

		String userName = data.getUser_name();
		String otp = data.getOtp();
		String password = data.getPassword();

		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(otp) || StringUtils.isEmpty(password) || StringUtils
				.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else {
			return new JSONObject().put("token",
					"eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE2MTU4OSwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRQYS5zYTBpQm96MVQzVUVxWksualF1NEYzcEZMaHovci5vWXBTZTcvMFMvSkhFeHJ2cFFTUyIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkdWRhSzBpbFpOS0R5dkQ5RzNtQTdCdWJtUE1rek4xWlF4UG4ubS9vNlVscVA4ZkdLMnBPcm0iLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.bf4I1EQz812SmVa8twH6gF-BNE2QeAK-N1234567890")
					.toString();
		}
	}

	@ApiResponses(value = { @ApiResponse(code = 200, message = "token retrieved succesfully"),
			@ApiResponse(code = 401, message = "You are not authorized to get token"), })
	@PostMapping(value = "/auth/basic/token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getToken(
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@RequestBody String data) throws IOException {
		Map<String, String> tokenMap = new ObjectMapper().readValue(data, new TypeReference<Map<String, String>>() {
		});
		String type = tokenMap.get("type");
		if (StringUtils.isEmpty(type) || StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER
				.getValue())) {
			throw new UnauthorizedException();
		} else {
			return new JSONObject().put("token",
					"eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE2MTU4OSwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRQYS5zYTBpQm96MVQzVUVxWksualF1NEYzcEZMaHovci5vWXBTZTcvMFMvSkhFeHJ2cFFTUyIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkdWRhSzBpbFpOS0R5dkQ5RzNtQTdCdWJtUE1rek4xWlF4UG4ubS9vNlVscVA4ZkdLMnBPcm0iLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.bf4I1EQz812SmVa8twH6gF-BNE2QeAK-N1234567890")
					.toString();
		}
	}

	@PutMapping(value = "/files/stream", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<String> upload(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @RequestHeader(required = false) String filename,
			InputStream fileStream) throws IOException {
		log.info("upload");

		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(filename)) {
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

	@GetMapping(value = "/files/resumables", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody()
	public ResponseEntity<String> getResumableUploads(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization)
			throws IOException {
		log.info("upload");

		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		}
		ResumableUploads resumableChunks = readResumableChunks(project);
		return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(resumableChunks));
	}

	@PatchMapping(value = "/files/stream/{filename}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<String> initializeResumableUpload(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @PathVariable String filename,
			@ApiParam(value = "chunk", example = "1") @RequestParam String chunk,
			@RequestParam(value = "id", required = false) String id,
			@RequestBody(required = false) byte[] content) throws IOException {
		log.info("upload chunk");
		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(filename)) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}
		if (id == null) {
			id = generateUploadID();
		}
		File uploadFolder = generateUploadFolder(String.format(durableFileImport, project), id);
		ResumableUploads resumableChunks = readResumableChunks(project);

		Chunk newChunk = createChunk(filename, chunk, id);
		ResumableUpload resumableUpload;

		if (chunk.equals("1")) {
			log.info("initializing chunk");
			File chunkFile = saveChunk(uploadFolder, filename, content);
			resumableUpload = updateResumableUpload(createReumableUpload(newChunk), chunkFile);
			resumableChunks.getResumables().add(resumableUpload);
		} else if ("end".equalsIgnoreCase(chunk)) {
			log.info("finalizing chunk");
			finalizeChunks(uploadFolder, id, resumableChunks);
		} else {
			log.info("Upload chunks");
			resumableUpload = getResumableUpload(id, resumableChunks);
			BigInteger maxChunk = new BigInteger(chunk);
			if (!maxChunk.subtract(resumableUpload.getMaxChunk()).equals(BigInteger.ONE)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createJsonMessage("chunk_order_incorrect"));
			}
			resumableUpload.setMaxChunk(maxChunk);
			File chunkFile = saveChunk(uploadFolder, chunk, resumableUpload.getFileName(), content);
			updateResumableUpload(resumableUpload, chunkFile);
			log.info(resumableChunks.toString());
			updateResumableChunks(resumableChunks, resumableUpload, newChunk);
			log.info(resumableChunks.toString());
		}

		log.info("write Resumable");
		try (FileWriter writer = new FileWriter(getResumablesPath(project))) {
			log.info(resumableChunks.toString());
			gson.toJson(resumableChunks, writer);
		} catch (IOException e) {
			log.error(e.getMessage());
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
		resumableUpload.setMd5Sum(DigestUtils.md5DigestAsHex((new FileInputStream(chunkFile))));
		return resumableUpload;
	}

	private ResumableUploads updateResumableChunks(ResumableUploads resumableChunks, ResumableUpload resumableUpload,
			Chunk newChunk) {
		resumableUpload.setMaxChunk(new BigInteger(newChunk.getMaxChunk()));
		List<ResumableUpload> newItems = resumableChunks.getResumables()
				.stream()
				.map(o -> o.getId().equals(resumableUpload.getId()) ? resumableUpload : o)
				.collect(Collectors.toList());
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
		return resumableChunks.getResumables()
				.stream()
				.filter(u -> u.getId().equals(id))
				.findAny()
				.get();
	}

	private String getResumablesPath(String project) {
		return String.format(durableFileImport.concat("/resumables.json"), project);
	}

	private ResumableUploads readResumableChunks(String project) {
		ResumableUploads resumables = new ResumableUploads();
		File resumablesPAth = new File(getResumablesPath(project));
		if (resumablesPAth.exists()) {
			log.info("read resumable");
			try (Reader reader = new FileReader(resumablesPAth)) {
				resumables.setResumables(gson.fromJson(reader, ResumableUploads.class).getResumables());
				log.info((resumables.toString()));
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return resumables;
	}

	private File saveChunk(File uploadFolder, String chunk, String filename, byte[] content) {
		File chunkFile = new File(uploadFolder, String.format(filename.concat(".chunk.%s"), chunk));
		try (FileWriter f = new FileWriter(chunkFile, true);
				BufferedWriter b = new BufferedWriter(f);
				PrintWriter p = new PrintWriter(b);) {

			p.println(content);

		} catch (IOException i) {
			log.error(i.getMessage());
		}
		log.info(chunkFile.getAbsolutePath());
		return chunkFile;
	}

	private void finalizeChunks(File uploadFolder, String id, ResumableUploads resumableChunks) {

		ResumableUpload resumable = getResumableUpload(id, resumableChunks);
		try {
			mergeFiles(uploadFolder, resumable);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		resumableChunks.setResumables(resumableChunks
				.getResumables()
				.stream()
				.filter(not((r -> r.getId().equals(id))))
				.collect(Collectors.toList()));
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

	private String createJsonMessage(String message) {
		return new JSONObject().put("message", message).toString();
	}

	private void mergeFiles(File dir, ResumableUpload resumable) throws IOException {
		String fileName = resumable.getFileName();
		try (PrintWriter pw = new PrintWriter(new File(String.format(durableFileImport, PROJECT), fileName))) {

			for (int i = 1; i <= resumable.getMaxChunk().intValue(); i++) {
				File chunkFile = new File(dir, String.format(fileName.concat(".chunk.%s"), i));

				log.info("Reading from " + chunkFile);
				try (
						BufferedReader br = new BufferedReader(new FileReader(chunkFile))) {
					Files.delete(chunkFile.toPath());
					// Read from current file
					String line = br.readLine();
					while (line != null) {
						// write to the output file
						pw.print(line);
						line = br.readLine();
					}
				}
			}
		}
		Files.delete(dir.toPath());
	}

}
