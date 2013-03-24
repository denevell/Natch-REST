package org.denevell.natch.tests.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.denevell.natch.login.LoginResourceInput;
import org.denevell.natch.login.LoginResourceReturnData;
import org.denevell.natch.register.RegisterResourceInput;
import org.denevell.natch.register.RegisterResourceReturnData;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;

public class LoginFunctional {
	
	private WebResource service;

	@Before
	public void setup() throws IOException, InterruptedException {
		service = TestUtils.getRESTClient();
		service
	    	.path("rest")
	    	.path("user")
	    	.delete();
	}
	
	@Test
	public void shouldLoginWithGoodCredentials() {
		// Arrange 
	    RegisterResourceInput registerInput = new RegisterResourceInput("aaron@aaron.com", "passy");
	    LoginResourceInput loginInput = new LoginResourceInput("aaron@aaron.com", "passy");
		service
	    	.path("rest").path("user").type(MediaType.APPLICATION_JSON)
	    	.put(RegisterResourceReturnData.class, registerInput);
	    
	    // Act
		LoginResourceReturnData loginResult = service
	    		.path("rest").path("login")
	    		.type(MediaType.APPLICATION_JSON)
	    		.post(LoginResourceReturnData.class, loginInput);
		
		// Assert
		assertEquals("", loginResult.getError());
		assertTrue("Should return true as 'successful' field", loginResult.isSuccessful());
	}
	
	@Test
	public void shouldSeeJsonErrorOnBadCredentials() {
		// Arrange 
	    RegisterResourceInput registerInput = new RegisterResourceInput("aaron@aaron.com", "passy");
	    LoginResourceInput loginInput = new LoginResourceInput("aaron@aaron.com", "passyWRONG");
		service
	    	.path("rest").path("user").type(MediaType.APPLICATION_JSON)
	    	.put(RegisterResourceReturnData.class, registerInput);
	    
	    // Act
		LoginResourceReturnData loginResult = service
	    		.path("rest").path("login")
	    		.type(MediaType.APPLICATION_JSON)
	    		.post(LoginResourceReturnData.class, loginInput);
		
		// Assert
		assertEquals("Incorrect username or password.", loginResult.getError());
		assertFalse("Should return true as 'successful' field", loginResult.isSuccessful());
	}
	
	public void login_shouldSeeJsonErrorOnBlanksPassed() {
		
	}
	
	public void login_shouldSeeJsonErrorOnBadJson() {
		
	}
	
	public void login_shouldBeAbleToLoginTwice() {
		
	}
	
}