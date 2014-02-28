package org.denevell.natch.tests.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ResourceBundle;

import org.denevell.natch.io.posts.AddPostResourceReturnData;
import org.denevell.natch.io.posts.ListPostsResource;
import org.denevell.natch.io.users.LoginResourceReturnData;
import org.denevell.natch.tests.functional.pageobjects.AddPostPO;
import org.denevell.natch.tests.functional.pageobjects.AddThreadFromPostPO;
import org.denevell.natch.tests.functional.pageobjects.LoginPO;
import org.denevell.natch.tests.functional.pageobjects.RegisterPO;
import org.denevell.natch.utils.Strings;
import org.denevell.natch.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class AddThreadFromMovedPostFunctional {
	
	private WebResource service;
    ResourceBundle rb = Strings.getMainResourceBundle();
	private LoginResourceReturnData adminLoginResult;
	private RegisterPO registerPo;
	private AddPostPO addPostPo;
	private LoginPO loginPo;
	private AddThreadFromPostPO addThreadFromPostPo;
	
	@Before
	public void setup() throws Exception {
		service = TestUtils.getRESTClient();
		registerPo = new RegisterPO(service);
		loginPo = new LoginPO(service);
		addPostPo = new AddPostPO(service);
		addThreadFromPostPo = new AddThreadFromPostPO(service);
		TestUtils.deleteTestDb();
	    new RegisterPO(service).register("aaron", "aaron");
		adminLoginResult = new LoginPO(service).login("aaron", "aaron");
	}
	
	@Test
	public void shouldMakeThreadFromPost() {
	    registerPo.register("other", "other");
		LoginResourceReturnData loginResult = 
				loginPo.login("other", "other");
		AddPostResourceReturnData threadRet = 
				addPostPo.add("c", "s", loginResult.getAuthKey());
		addPostPo.add("-", "b", loginResult.getAuthKey(), threadRet.getThread().getId());
		ListPostsResource posts = 
				ListPostsFunctional.listRecentPostsThreads(service);
		assertTrue("Should have two posts, thread starter and first post", posts.getPosts().size()==2);
		
        // Act
		AddPostResourceReturnData returnData = 
				addThreadFromPostPo.addThreadFromPost(
						"New subject",
						"b",
						posts.getPosts().get(0).getId(),
						"other",
						adminLoginResult.getAuthKey()); 
		
		// Assert
		assertTrue(returnData.isSuccessful());
		posts = ListPostsFunctional.listRecentPostsThreads(service);
		assertEquals("New thread has right subject", posts.getPosts().get(0).getSubject(), "New subject");
		assertEquals("New thread has right content", posts.getPosts().get(0).getContent(), "b");
		assertTrue("Still just have two posts, since one's been moved", posts.getPosts().size()==2);
	}

	@Test
	public void shouldThrow401WhenNotAdmin() {
	    registerPo.register("other", "other");
		LoginResourceReturnData loginResult = 
				loginPo.login("other", "other");
		AddPostResourceReturnData threadRet = 
				addPostPo.add("c", "s", loginResult.getAuthKey());
		addPostPo.add("-", "b", loginResult.getAuthKey(), threadRet.getThread().getId());
		ListPostsResource posts = ListPostsFunctional.listRecentPostsThreads(service);
		
        // Act
		try {
				addThreadFromPostPo.addThreadFromPost(
						"New subject",
						"b",
						posts.getPosts().get(0).getId(),
						"other",
						loginResult.getAuthKey()); 
        } catch (UniformInterfaceException e) {
            assertTrue(e.getResponse().getStatus()==401);
            return;
        }
		assertFalse("Was exception 401 exception", true);
	}
	
	// TODO: 401 when not an admin
	
}