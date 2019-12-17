package no.uio.ifi.tsd.controller;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import no.uio.ifi.tsd.model.ResumableUploadRepository;
import no.uio.ifi.tsd.model.ResumableUploads;
import no.uio.ifi.tsd.model.User;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import io.jsonwebtoken.*;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/v1/{project}")
@Slf4j
@Api(value = "TSD File Api Stub")
public class TSDStubController {

	private static final int ONE_HOUR = 3600000;
	private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE2MTU4OSwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRQYS5zYTBpQm96MVQzVUVxWksualF1NEYzcEZMaHovci5vWXBTZTcvMFMvSkhFeHJ2cFFTUyIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkdWRhSzBpbFpOS0R5dkQ5RzNtQTdCdWJtUE1rek4xWlF4UG4ubS9vNlVscVA4ZkdLMnBPcm0iLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.bf4I1EQz812SmVa8twH6gF-BNE2QeAK-N1234567890";
	private static final String DELETING = "deleting ";
	public static final String CANNOT_DELETE_RESUMABLE = "cannot delete resumable";
	public static final String RESUMABLE_DELETED = "resumable deleted";
	public static final String STREAM_PROCESSING_FAILED = "stream processing failed";
	public static final String CHUNK_ORDER_INCORRECT = "chunk_order_incorrect";
	public static final String DATA_STREAMED = "data streamed";
	public static final String PROJECT = "p11";

	private Gson gson = new Gson();

	@Value("${tsd.file.import}")
	public String durableFileImport;

	public static String SECRET_KEY;

	@Value("${tsd.file.secretkey}")
	public void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

	@Autowired
	private ResumableUploadRepository repository;

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
			String jwtToken = createJWT(userName, "TSD", userName, ONE_HOUR);
			return new JSONObject().put("token", jwtToken).toString();
		}
	}

	@ApiResponses(value = { @ApiResponse(code = 200, message = "token retrieved succesfully"),
			@ApiResponse(code = 401, message = "You are not authorized to get token"), })
	@PostMapping(value = "/auth/basic/token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getToken(
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@RequestBody String data) throws IOException {
		Map<String, String> tokenMap = new ObjectMapper().readValue(data, new TypeReference<>() {
		});
		String type = tokenMap.get("type");
		if (StringUtils.isEmpty(type) || StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER
				.getValue())) {
			throw new UnauthorizedException();
		} else {
			return new JSONObject().put("token", TOKEN).toString();
		}
	}

	@PutMapping(value = "/files/stream", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<String> upload(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @RequestHeader(required = false, name = "filename") String fileName,
			InputStream fileStream) throws IOException {
		log.info("upload");

		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(fileName)) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}
		Path path = Paths.get(String.format(durableFileImport, project), fileName);
		try {
			Files.copy(fileStream, path, StandardCopyOption.REPLACE_EXISTING);
			log.info(path.getParent().toString());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(createJsonMessage(DATA_STREAMED));
	}

	@PutMapping(value = "/files/folder", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody()
	public ResponseEntity<String> createFolder(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FolderName", example = "name") @RequestParam(required = true, name = "name") String name) {
		log.info("create folder ");

		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(name)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createJsonMessage("miising name"));
		}

		Path path = Paths.get(String.format(durableFileImport, project), name);
		try {
			Files.createDirectory(path);
			log.info("created: " + path.toString());
		} catch (IOException e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createJsonMessage(e.getClass().getTypeName() + e.getMessage()));
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(createJsonMessage("folder created"));
	}

	@GetMapping(value = "/files/resumables", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody()
	public ResponseEntity<String> getResumableUploads(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization) {
		log.info("upload");

		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		}
		ResumableUploads resumableChunks = readResumableChunks();
		return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(resumableChunks));
	}

	@PatchMapping(value = "/files/stream/{filename}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody()
	public ResponseEntity<String> handleResumableUpload(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @PathVariable(name = "filename") String fileName,
			@ApiParam(value = "chunk", example = "1") @RequestParam String chunk,
			@RequestParam(value = "id", required = false) String id,
			@RequestBody(required = false) byte[] content) throws IOException {
		log.info("upload chunk");
		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(fileName)) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}
		if (id == null) {
			id = repository.save(new ResumableUpload()).getId();
		}
		File uploadFolder = generateUploadFolder(String.format(durableFileImport, project), id);
		ResumableUploads resumableChunks = readResumableChunks();

		Chunk newChunk = createChunk(fileName, chunk, id);
		ResumableUpload resumableUpload;

		if (chunk.equals("1")) {
			log.info("initializing chunk");
			File chunkFile = saveChunk(uploadFolder, fileName, content);
			resumableUpload = updateResumableUpload(createResumableUpload(newChunk), chunkFile);
			resumableChunks.getResumables().add(resumableUpload);
		} else if ("end".equalsIgnoreCase(chunk)) {
			log.info("finalizing chunk");
			finalizeChunks(uploadFolder, id, resumableChunks, project);
		} else {
			log.info("Upload chunks");
			resumableUpload = getResumableUpload(id);
			BigInteger maxChunk = new BigInteger(chunk);
			if (!maxChunk.subtract(resumableUpload.getMaxChunk()).equals(BigInteger.ONE)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createJsonMessage(CHUNK_ORDER_INCORRECT));
			}
			resumableUpload.setMaxChunk(maxChunk);
			File chunkFile = saveChunk(uploadFolder, chunk, resumableUpload.getFileName(), content);
			updateResumableUpload(resumableUpload, chunkFile);
			updateResumableChunks(resumableChunks, resumableUpload, newChunk);
			log.info(resumableChunks.toString());
		}

		log.info(gson.toJson(newChunk));
		return ResponseEntity.status(HttpStatus.CREATED).body(gson.toJson(newChunk));
	}

	@DeleteMapping(value = "/files/resumables/{filename}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody()
	public ResponseEntity<String> deleteResumableUpload(
			@ApiParam(value = "project ID ", required = true, example = PROJECT) @PathVariable String project,
			@ApiParam(value = "Authorization of type bearer", example = "Bearer tokensdgdfgdfgfdg") @RequestHeader(required = false) String authorization,
			@ApiParam(value = "FileName", example = "name.ext") @PathVariable(name = "filename") String fileName,
			@RequestParam(value = "id", required = false) String id) {
		log.info("upload chunk");
		if (StringUtils.isEmpty(authorization) || !authorization.startsWith(BEARER.getValue())) {
			throw new UnauthorizedException();
		} else if (StringUtils.isEmpty(fileName)) {
			return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(STREAM_PROCESSING_FAILED));
		}
		if (StringUtils.isEmpty(id)) {
			return badRequestCannotDelete();
		}
		File uploadFolder = generateUploadFolder(String.format(durableFileImport, project), id);

		try {
			ResumableUpload resumableUpload = getResumableUpload(id);
			deleteFiles(uploadFolder, resumableUpload);
			repository.delete(resumableUpload);
		} catch (Exception e) {
			return badRequestCannotDelete();
		}

		return ResponseEntity.status(HttpStatus.OK).body(createJsonMessage(RESUMABLE_DELETED));
	}

	private ResponseEntity<String> badRequestCannotDelete() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createJsonMessage(CANNOT_DELETE_RESUMABLE));
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
		resumableUpload.setMd5Sum(DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(chunkFile
				.getAbsolutePath()))));
		repository.save(resumableUpload);
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

	private Chunk createChunk(String fileName, String chunk, String id) {
		Chunk newChunk = new Chunk();
		newChunk.setFileName(fileName);
		newChunk.setId(id);
		newChunk.setMaxChunk(chunk);
		return newChunk;
	}

	private ResumableUpload createResumableUpload(Chunk newChunk) {
		ResumableUpload resumableUpload = new ResumableUpload();
		resumableUpload.setFileName(newChunk.getFileName());
		resumableUpload.setId(newChunk.getId());
		resumableUpload.setPreviousOffset(BigInteger.ZERO);
		resumableUpload.setMaxChunk(new BigInteger(newChunk.getMaxChunk()));
		return resumableUpload;
	}

	private ResumableUpload getResumableUpload(String id) {
		return repository.findById(id).orElseThrow();
	}

	private ResumableUploads readResumableChunks() {
		ResumableUploads resumables = new ResumableUploads();
		resumables.setResumables((List<ResumableUpload>) repository.findAll());
		return resumables;
	}

	private File saveChunk(File uploadFolder, String chunk, String fileName, byte[] content) throws IOException {
		File chunkFile = createChunkFile(uploadFolder, fileName, Integer.parseInt(chunk));
		log.info("Saving chunk " + chunkFile.getName() + " to " + uploadFolder.getCanonicalPath());
		Files.write(chunkFile.toPath(), content);
		log.info(chunkFile.getAbsolutePath());
		return chunkFile;
	}

	private File saveChunk(File uploadFolder, String fileName, byte[] content) throws IOException {
		return saveChunk(uploadFolder, "1", fileName, content);
	}

	private void finalizeChunks(File uploadFolder, String id, ResumableUploads resumableChunks, String project)
			throws IOException {
		ResumableUpload resumable = getResumableUpload(id);
		try {
			mergeFiles(uploadFolder, resumable, project);
		} catch (IOException e) {
			log.error(e.getMessage());
			throw e;
		}
		repository.deleteById(id);
		resumableChunks.setResumables((List<ResumableUpload>) repository.findAll());
	}

	private File generateUploadFolder(String path, String uploadId) {
		File uploadDir = new File(path, uploadId);
		if (!uploadDir.exists()) {
			log.info(uploadDir + " not exist");
			if (uploadDir.mkdirs()) {
				log.info(uploadDir + " created");
			}
		} else {
			log.info(uploadDir + " already exist");
		}
		return uploadDir;
	}

	private void mergeFiles(File dir, ResumableUpload resumable, String project) throws IOException {
		String fileName = resumable.getFileName();
		File uploadedFile = new File(String.format(durableFileImport, project), fileName);
		try (OutputStream outputStream = Files.newOutputStream(Paths.get(uploadedFile.getAbsolutePath()),
				StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
			for (int i = 1; i <= resumable.getMaxChunk().intValue(); i++) {
				File chunkFile = createChunkFile(dir, fileName, i);
				log.info("Reading from " + chunkFile);
				try (InputStream inputStream = Files.newInputStream(Paths.get(chunkFile.getAbsolutePath()))) {
					log.info("writing to file " + uploadedFile.getAbsolutePath());
					IOUtils.copy(inputStream, outputStream);
				}
				log.info(DELETING + chunkFile.toPath());
				Files.delete(chunkFile.toPath());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		Files.delete(dir.toPath());
	}

	private File createChunkFile(File dir, String fileName, int i) {
		return new File(dir, String.format(fileName.concat(".chunk.%s"), i));
	}

	private void deleteFiles(File dir, ResumableUpload resumable) throws IOException {
		String fileName = resumable.getFileName();
		for (int i = 1; i <= resumable.getMaxChunk().intValue(); i++) {
			File chunkFile = createChunkFile(dir, fileName, i);
			log.info(DELETING + chunkFile.toPath());
			Files.delete(chunkFile.toPath());
		}
		log.info(DELETING + dir.toPath());
		Files.delete(dir.toPath());
	}

	private String createJsonMessage(String message) {
		return new JSONObject().put("message", message).toString();
	}

	public static String createJWT(String id, String issuer, String subject, long ttlMillis) {

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		JwtBuilder builder = Jwts.builder()
				.setId(id)
				.setIssuedAt(now)
				.setSubject(subject)
				.setIssuer(issuer)
				.signWith(signatureAlgorithm, signingKey);

		if (ttlMillis > 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		return builder.compact();
	}

	public static Claims decodeJWT(String jwt) {
		Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
				.parseClaimsJws(jwt)
				.getBody();
		return claims;
	}
}
