package no.uio.ifi.tsd;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import no.uio.ifi.tsd.enums.TokenType;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TSDStubControllerTests {

    @Autowired
    private MockMvc mockMvc;

    
    @Test
    public void givenFullRequestwhenTSDThenToken() throws Exception {
    	
    	this.mockMvc.perform(post("/v1/p11/auth/tsd/token")
    			.param("type", TokenType.IMPORT.name())
    			.header("authorization", "Bearer token")
    			.header("Content-Type", MediaType.APPLICATION_JSON)
    			.content("{" + 
    					"\"user_name\": \"p11-user123\"," + 
    					"\"otp\": \"113943\"," + 
    					"\"password\": \"password123456\"" + 
    					"}"))
    	.andDo(print())
    	.andExpect(status().isOk())
    	.andExpect(jsonPath("$.token").exists());
    }
    
    @Test
    public void givenMissingOTPWhenTSDThenUnauthorized() throws Exception {
    	
    	this.mockMvc.perform(post("/v1/p11/auth/tsd/token")
    			.param("type", TokenType.IMPORT.name())
    			.header("authorization", "Bearer token")
    			.header("Content-Type", MediaType.APPLICATION_JSON)
    			.content("{" + 
    					"\"user_name\": \"p11-user123\"," + 
    					"\"password\": \"password123456\"" + 
    					"}"))
    	.andDo(print())
    	.andExpect(status().isUnauthorized())
    	.andExpect(jsonPath("$.message").value("Authentication failed"));
    }
    
    @Test
    public void givenEmptyAuthorizationWhenTSDThenUnauthorized() throws Exception {
    	
    	this.mockMvc.perform(post("/v1/p11/auth/tsd/token")
    			.param("type", TokenType.IMPORT.name())
    			.header("authorization", "")
    			.header("Content-Type", MediaType.APPLICATION_JSON)
    			.content("{" + 
    					"\"user_name\": \"p11-user123\"," + 
    					"\"otp\": \"113943\"," + 
    					"\"password\": \"password123456\"" + 
    					"}"))
    	.andDo(print())
    	.andExpect(status().isUnauthorized())
    	.andExpect(jsonPath("$.message").value("Authentication failed"));
    }
    
    @Test
    public void givenMissingAuthorizationWhenTSDThenUnauthorized() throws Exception {

        this.mockMvc.perform(post("/v1/p11/auth/tsd/token")
        		.param("type", TokenType.IMPORT.name())
        		.header("Content-Type", MediaType.APPLICATION_JSON)
        		.content("{" + 
        				"\"user_name\": \"p11-user123\"," + 
        				"\"otp\": \"113943\"," + 
        				"\"password\": \"password123456\"" + 
        				"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }

}
