package no.uio.ifi.tsd.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.tsd.enums.TokenType;
import no.uio.ifi.tsd.exception.UnauthorizedException;
import no.uio.ifi.tsd.model.User;

@RestController
@RequestMapping("/v1/{project}")
@Slf4j
@Api(value = "TSD File Api Stub")
public class TSDStubController {

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

	private String createJsonMessage(String message) throws JSONException {
		return new JSONObject().put("message", message).toString();
	}
}
