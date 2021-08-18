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
public class TokenTest {
	
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;
    
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testTokenGet() throws Exception {
//    	mockMvc.perform((get("/token/get")
//    			.param("callback", "admin")
//    			.param("session_id", "1")))
//        .andDo(print());
        
        String responseString = mockMvc.perform(
	                get("/token/get")
	                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	                .param("callback","callback_"+System.currentTimeMillis())
	                .param("session_id", StringUtils.hex62EncodingWithRandom(32))
        		).andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
    	Assert.assertNotNull(responseString);
        System.out.println("testTokenGet   :"+responseString);
    }
}
