package no.uio.ifi.tsd;

import static no.uio.ifi.tsd.controller.TSDStubController.PROJECT;
import static no.uio.ifi.tsd.controller.TSDStubController.TSD_S_DATA_DURABLE_FILE_IMPORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.google.gson.Gson;

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
	private static final String API_PROJECT = "https://api.tsd.usit.no/v1/p11";
	@Autowired
	private MockMvc mockMvc;

	private Gson gson = new Gson();

	@BeforeAll
	public static void setup() {
		File resumables = new File(String.format(TSD_S_DATA_DURABLE_FILE_IMPORT, PROJECT));
		if (resumables.exists()) {
			try {
				FileUtils.deleteDirectory(resumables);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void givenFullRequestwhenTSDThenToken() throws Exception {
		
		this.mockMvc
		.perform(post(API_PROJECT + "/auth/tsd/token").param("type", TokenType.IMPORT.name())
				.header("authorization", "Bearer token")
				.header("Content-Type", MediaType.APPLICATION_JSON)
				.content("{" + "\"user_name\": \"p11-user123\"," + "\"otp\": \"113943\","
						+ "\"password\": \"password123456\"" + "}"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.token").exists());
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
						//.content(readBytes(testFile))
						.header("authorization", TOKEN))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.max_chunk").value("end"))
				.andExpect(jsonPath("$.id").value(chunk.getId()))
				.andExpect(jsonPath("$.filename").value(testFile.getName()));
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

	private File createTempFile() throws IOException {
		File testFile = File.createTempFile(new Random().nextInt()+"", "fdf");
		FileWriter writer = new FileWriter(testFile);

		while (testFile.length() <= 1) {
			writer.write("abcdefghijkl\n");
		}
		writer.flush();
		writer.close();
		return testFile;
	}

	private byte[] readBytes(File testFile) throws IOException, FileNotFoundException {
		return new FileInputStream(testFile).readAllBytes();
	}

}
