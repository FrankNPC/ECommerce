package ecommerce.rest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.common.StringUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//SWITCHER, test local or cloud, commented for cloud, included for local
import org.springframework.test.context.ActiveProfiles;
@ActiveProfiles("local")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RestBootApplication.class)
@WebAppConfiguration
public class UserTest {
	
	private MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext wac;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	private String getToken(String sessionId) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String responseString = mockMvc.perform(
					get("/token/get")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//					.param("callback","callback_"+System.currentTimeMillis())
					.param("session_id", sessionId)
				).andExpect(status().isOk())
				.andDo(print())
				.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(responseString);
		System.out.println("getToken		   :"+responseString);
		return objectMapper.readTree(responseString).get("token").asText();
	}
	@Test
	public void testUser() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String username = "user_"+System.currentTimeMillis();
		String password = StringUtils.hex62EncodingWithRandom(32);
		String responseString = mockMvc.perform(
					get("/user/create")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//					.param("callback","callback_"+System.currentTimeMillis())
					.param("username", username)
					.param("password", password)
					.param("token", getToken(null))
				).andExpect(status().isOk())
				.andDo(print())
				.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(objectMapper.readTree(responseString).get("session_id"));
		System.out.println("testUsercreate	 :"+responseString);
		

		responseString = mockMvc.perform(
					get("/user/login")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//					.param("callback","callback_"+System.currentTimeMillis())
					.param("username", username)
					.param("password", password)
					.param("token", objectMapper.readTree(responseString).get("token").asText())
				).andExpect(status().isOk())
				.andDo(print())
				.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(objectMapper.readTree(responseString).get("session_id"));
		System.out.println("testLogin		  :"+responseString);

		responseString = mockMvc.perform(
					get("/user/reset")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//					.param("callback","callback_"+System.currentTimeMillis())
					.param("session_id", objectMapper.readTree(responseString).get("session_id").asText())
					.param("token", objectMapper.readTree(responseString).get("token").asText())
				).andExpect(status().isOk())
				.andDo(print())
				.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(objectMapper.readTree(responseString).get("session_id"));
		System.out.println("testReset		  :"+responseString);

		responseString = mockMvc.perform(
				get("/user/reset")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
//				.param("callback","callback_"+System.currentTimeMillis())
				.param("password", StringUtils.hex62EncodingWithRandom(32))
				.param("session_id", objectMapper.readTree(responseString).get("session_id").asText())
				.param("token", objectMapper.readTree(responseString).get("token").asText())
			).andExpect(status().isOk())
			.andDo(print())
			.andReturn().getResponse().getContentAsString();
		Assert.assertNotNull(objectMapper.readTree(responseString).get("session_id"));
		System.out.println("testReset		  :"+responseString);
	}
}
