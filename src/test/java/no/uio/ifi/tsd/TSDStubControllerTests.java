package no.uio.ifi.tsd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import no.uio.ifi.tsd.controller.TSDStubController;
import no.uio.ifi.tsd.enums.TokenType;
import no.uio.ifi.tsd.model.Chunk;
import no.uio.ifi.tsd.model.ResumableUpload;
import no.uio.ifi.tsd.model.ResumableUploads;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class TSDStubControllerTests {

	private static final String TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJlaWQiOm51bGwsImV4cCI6MTU3NDE3NzUwMiwiZ3JvdXBzIjpbInAxMS1hbWdhZHNoLWdyb3VwIiwicDExLW1lbWJlci1ncm91cCIsInAxMS1leHBvcnQtZ3JvdXAiXSwicGlkIjpudWxsLCJwcm9qIjoicDExIiwiciI6IiQyYiQxMiRHQ3c4MUJYQ0JkMFcwUldwRWtrNmtPcTJ5Sm5VMzIud2VJRnYxbWxVWlA3a083UW1HbFVxLiIsInJvbGUiOiJpbXBvcnRfdXNlciIsInUiOiIkMmIkMTIkd2NibGRVaVJLcE1haTUxZ3Vld0hHLi5VNlBPbEV0cUl2V0RkWnBkbC55SWRTcHgwaDQuREsiLCJ1c2VyIjoicDExLWFtZ2Fkc2gifQ.IWwbjrr1AVMThLErPqOzBs5Oo_9oRaLUcLKnozpzdiw";
	private static final String PROJECT = "p13";
	private static final String API_PROJECT = "https://api.tsd.usit.no/v1/" + PROJECT;
	@Autowired
	private MockMvc mockMvc;
	@Value("${tsd.file.import}")
	private String durableFileImport;
	private Gson gson = new Gson();

	private String userName;
	private String email;

	@BeforeEach
	public void setup() throws UnsupportedEncodingException, Exception {
		File resumables = new File(String.format(durableFileImport, PROJECT));
		if (resumables.exists()) {
			try {
				FileUtils.cleanDirectory(resumables);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		char[] possibleCharacters = (new String("abcdefghijklmnopqrstuvwxyz0123456789")).toCharArray();
		int passwordLength = 8;
		userName = RandomStringUtils.random(passwordLength, 0, possibleCharacters.length - 1, false,
				false,
				possibleCharacters, new SecureRandom());
		email = userName;
	}

	@Test
	public void givenFullRequestwhenSignupThenClientId() throws Exception {

		ResultActions resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signup")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"email\": \"" + userName + "\","
								+ "\"client_name\": \"client1\"" + "}"));
		resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client_id").exists())
				.andReturn();
	}

	@Test
	public void givenFullRequestwhenSignupConfirmThenClientId() throws Exception {
		ResultActions resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signup")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"email\": \"" + userName + "\","
								+ "\"client_name\": \"client1\"" + "}"));
		MvcResult result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client_id").exists())
				.andReturn();

		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signupconfirm")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{"
								+ "\"email\": \"" + userName + "\","
								+ "\"client_id\": \"" + JsonPath.read(result.getResponse().getContentAsString(),
										"$.client_id") + "\","
								+ "\"client_name\": \"client1\""
								+ "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.confirmation_token").exists())
				.andReturn();
	}

	@Test
	public void givenFullRequestwhenConfirmThenClientId() throws Exception {
		ResultActions resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signup")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"email\": \"" + userName + "\","
								+ "\"client_name\": \"client1\"" + "}"));
		MvcResult result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client_id").exists())
				.andReturn();

		Object clientId = JsonPath.read(result.getResponse().getContentAsString(),
				"$.client_id");
		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signupconfirm")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{"
								+ "\"email\": \"" + userName + "\","
								+ "\"client_id\": \"" + clientId + "\","
								+ "\"client_name\": \"client1\""
								+ "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.confirmation_token").exists())
				.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/confirm")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{"
								+ "\"client_id\": \"" + clientId + "\","
								+ "\"confirmation_token\": \"" + JsonPath.read(contentAsString, "$.confirmation_token")
								+ "\""
								+ "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.password").exists())
				.andReturn();
	}

	@Test
	public void givenFullRequestwhenTSDThenToken() throws Exception {
		String apiKey = getToken();
		ResultActions resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/tsd/token").param("type", TokenType.IMPORT.name())
						.header("authorization", apiKey)
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"user_name\": \""
								+ userName
								+ "\"," + "\"otp\": \"113943\","
								+ "\"password\": \"password123456\"" + "}"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();
		String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
		Claims claims = TSDStubController.decodeJWT(token);
		assertEquals(userName, claims.getId());
		assertEquals("TSD", claims.getIssuer());
		assertEquals(userName, claims.getSubject());
	}

	@Test
	public void givenFullRequestwhenNoTSDThenToken() throws Exception {

		this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/token")
						.header("authorization", "Bearer token")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content(String.format("{\"type\":\"%s\"}", TokenType.IMPORT.name().toLowerCase())))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	public void givenMissingOTPWhenTSDThenUnauthorized() throws Exception {

		this.mockMvc
				.perform(post(API_PROJECT + "/auth/tsd/token").param("type", TokenType.IMPORT.name())
						.header("authorization", "Bearer token")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"user_name\": \"p11-user123\"," + "\"password\": \"password123456\"" + "}"))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	public void givenEmptyAuthorizationWhenTSDThenUnauthorized() throws Exception {

		this.mockMvc
				.perform(post(API_PROJECT + "/auth/tsd/token").param("type", TokenType.IMPORT.name())
						.header("authorization", "")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"user_name\": \"p11-user123\"," + "\"otp\": \"113943\","
								+ "\"password\": \"password123456\"" + "}"))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	public void givenMissingAuthorizationWhenTSDThenUnauthorized() throws Exception {

		this.mockMvc
				.perform(post(API_PROJECT + "/auth/tsd/token").param("type", TokenType.IMPORT.name())
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"user_name\": \"p11-user123\"," + "\"otp\": \"113943\","
								+ "\"password\": \"password123456\"" + "}"))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	public void givenFileWhenfileStreamThenTransmitted() throws Exception {
		File testFile = createTempFile();

		this.mockMvc
				.perform(put(API_PROJECT + "/files/stream").content(readBytes(testFile))
						.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
						.header("FileName", testFile.getName())
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value(TSDStubController.DATA_STREAMED));
	}

	@Test
	public void givenNameWhenFolderThenCreated() throws Exception {

		this.mockMvc
				.perform(put(API_PROJECT + "/files/folder")
						.header("authorization", TOKEN)
						.param("name", "newFolder"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("folder created"));
	}

	@Test
	public void givenNameWhenFolderTwiceThenCreated() throws Exception {

		this.mockMvc
				.perform(put(API_PROJECT + "/files/folder")
						.header("authorization", TOKEN)
						.param("name", "newFolder"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("folder created"));
		this.mockMvc
				.perform(put(API_PROJECT + "/files/folder")
						.header("authorization", TOKEN)
						.param("name", "newFolder"))
				.andDo(print())
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void givenMissingNameWhenFolderThenCreated() throws Exception {

		this.mockMvc
				.perform(put(API_PROJECT + "/files/folder")
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	public void givenNoAuthorizationWhenfileStreamThenAuthenticationFailed() throws Exception {
		File testFile = createTempFile();

		this.mockMvc
				.perform(put(API_PROJECT + "/files/stream").content(readBytes(testFile))
						.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
						.header("FileName", testFile.getName())
						.header("authorization", ""))
				.andDo(print())
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Authentication failed"));
	}

	@Test
	public void givenNoFileNameWhenfileStreamThenFailed() throws Exception {
		File testFile = createTempFile();

		this.mockMvc
				.perform(put(API_PROJECT + "/files/stream").content(readBytes(testFile))
						.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(TSDStubController.STREAM_PROCESSING_FAILED));
	}

	@Test
	public void givenChunkWhenfileStreamGetResumablesThenPass() throws Exception {
		File testFile = createTempFile();

		ResultActions result = this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=1")
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("1"))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		Chunk chunk = gson.fromJson(result.andReturn().getResponse().getContentAsString(), Chunk.class);
		String id = chunk.getId();
		result = this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=2&id=" + id)
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("2"))
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		chunk = gson.fromJson(result.andReturn().getResponse().getContentAsString(), Chunk.class);

		ResultActions resultActions = this.mockMvc
				.perform(get(API_PROJECT + "/files/resumables/")
						.param("project", PROJECT)
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isOk());
		ResumableUpload resumableUpload = gson.fromJson(resultActions.andReturn().getResponse().getContentAsString(),
				ResumableUploads.class)
				.getResumables()
				.stream()
				.filter(u -> u.getId().equals(id))
				.findAny()
				.get();
		assertEquals(chunk.getFileName(), resumableUpload.getFileName());
		assertEquals(chunk.getMaxChunk(), resumableUpload.getMaxChunk().toString());
	}

	@Test
	public void givenChunkWhenfileStreamChunkThenPass() throws Exception {
		File testFile = createTempFile();

		ResultActions result = this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=1")
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("1"))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		Chunk chunk = gson.fromJson(result.andReturn().getResponse().getContentAsString(), Chunk.class);
		this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=2&id=" + chunk.getId())
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("2"))
				.andExpect(jsonPath("$.id").value(chunk.getId()))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=end&id=" + chunk.getId())
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("end"))
				.andExpect(jsonPath("$.id").value(chunk.getId()))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
	}

	@Test
	public void givenChunkWhenfileStreamDeleteThenPass() throws Exception {
		File testFile = createTempFile();

		ResultActions result = this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=1")
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("1"))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		Chunk chunk = gson.fromJson(result.andReturn().getResponse().getContentAsString(), Chunk.class);
		this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=2&id=" + chunk.getId())
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("2"))
				.andExpect(jsonPath("$.id").value(chunk.getId()))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));

		this.mockMvc
				.perform(delete(API_PROJECT + "/files/resumables/" + testFile.getName() + "?id=" + chunk.getId())
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(TSDStubController.RESUMABLE_DELETED));

		ResultActions resultActions = this.mockMvc
				.perform(get(API_PROJECT + "/files/resumables/")
						.param("project", PROJECT)
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isOk());
		Optional<ResumableUpload> resumableUpload = gson.fromJson(resultActions.andReturn()
				.getResponse()
				.getContentAsString(),
				ResumableUploads.class)
				.getResumables()
				.stream()
				.filter(u -> u.getId().equals(chunk.getId()))
				.findAny();
		assertEquals(true, resumableUpload.isEmpty());

	}

	@Test
	public void givenWrongChunkWhenfileStreamDeleteThenBadRequest() throws Exception {
		File testFile = createTempFile();

		this.mockMvc
				.perform(delete(API_PROJECT + "/files/resumables/" + testFile.getName() + "?id=" + "xxx")
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(TSDStubController.CANNOT_DELETE_RESUMABLE));
	}

	@Test
	public void givenMissingIdWhenfileStreamDeleteThenBadRequest() throws Exception {
		File testFile = createTempFile();

		this.mockMvc
				.perform(delete(API_PROJECT + "/files/resumables/" + testFile.getName())
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(TSDStubController.CANNOT_DELETE_RESUMABLE));
	}

	@Test
	public void givenwrongChunkWhenfileStreamChunkThenFail() throws Exception {
		File testFile = createTempFile();

		ResultActions result = this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=1")
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("1"))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
		Chunk chunk = gson.fromJson(result.andReturn().getResponse().getContentAsString(), Chunk.class);
		this.mockMvc
				.perform(patch(API_PROJECT + "/files/stream/" + testFile.getName() + "?chunk=3&id=" + chunk.getId())
						.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("chunk_order_incorrect"));
	}

	private String getToken() throws Exception, UnsupportedEncodingException {
		ResultActions resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signup")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"client_name\": \"" + userName + "\","
								+ "\"email\": \"" + email + "\""
								+ "}"));
		MvcResult result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client_id").exists())
				.andReturn();
		String client_id = JsonPath.read(result.getResponse().getContentAsString(), "$.client_id");

		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/signupconfirm")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"client_name\": \"" + userName + "\","
								+ "\"client_id\": \"" + client_id + "\","
								+ "\"email\": \"" + email + "\"" + "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.confirmation_token").exists())
				.andReturn();
		String confirmation_token = JsonPath.read(result.getResponse().getContentAsString(),
				"$.confirmation_token");

		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/confirm")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"client_id\": \"" + client_id + "\","
								+ "\"confirmation_token\": \"" + confirmation_token + "\"" + "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.password").exists())
				.andReturn();
		String password = JsonPath.read(result.getResponse().getContentAsString(), "$.password");

		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/basic/api_key")
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"client_id\": \"" + client_id + "\","
								+ "\"password\": \"" + password + "\""
								+ "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.api_key").exists())
				.andReturn();
		String api_key = JsonPath.read(result.getResponse().getContentAsString(), "$.api_key");

		resultActions = this.mockMvc
				.perform(post(API_PROJECT + "/auth/tsd/token")
						.param("type", TokenType.IMPORT.name())
						.header("authorization", "Bearer "
								+ api_key)
						.header("Content-Type", MediaType.APPLICATION_JSON)
						.content("{" + "\"user_name\": \"" + userName + "\","
								+ "\"otp\": \"113943\","
								+ "\"password\": \"" + password + "\""
								+ "}"));
		result = resultActions
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();
		String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
		return BEARER.getValue() + " " + token;
	}

	private File createTempFile() throws IOException {
		File testFile = File.createTempFile(new Random().nextInt() + "", "fdf");
		FileWriter writer = new FileWriter(testFile);

		while (testFile.length() <= 1) {
			writer.write("abcdefghijkl");
		}
		writer.flush();
		writer.close();
		return testFile;
	}

	private byte[] readBytes(File testFile) throws IOException, FileNotFoundException {
		return new FileInputStream(testFile).readAllBytes();
	}

	private File createFile(final long sizeInBytes) throws IOException {
		File file = File.createTempFile(new Random().nextInt() + "", "fdf");

		log.info(file.getAbsolutePath());
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(sizeInBytes);
		raf.close();
		log.info("" + file.length());

		return file;
	}
}
