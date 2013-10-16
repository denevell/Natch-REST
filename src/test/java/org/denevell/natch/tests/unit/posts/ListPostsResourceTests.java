package org.denevell.natch.tests.unit.posts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.db.entities.ThreadEntity;
import org.denevell.natch.db.entities.UserEntity;
import org.denevell.natch.io.posts.ListPostsResource;
import org.denevell.natch.io.posts.PostResource;
import org.denevell.natch.io.threads.ThreadResource;
import org.denevell.natch.serv.post.show.ShowPostModel;
import org.denevell.natch.serv.post.show.SinglePostRequest;
import org.denevell.natch.serv.posts.list.ListPosts;
import org.denevell.natch.serv.posts.list.ListPostsModel;
import org.denevell.natch.serv.thread.list.ListThreadModel;
import org.denevell.natch.serv.thread.list.ListThreadRequest;
import org.denevell.natch.utils.Strings;
import org.junit.Before;
import org.junit.Test;

import scala.actors.threadpool.Arrays;

public class ListPostsResourceTests {
	
	private ListPostsModel postsModel;
    ResourceBundle rb = Strings.getMainResourceBundle();
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ListPosts resourceList;
	private ShowPostModel showPostsModel;

	@Before
	public void setup() {
		postsModel = mock(ListPostsModel.class);
		showPostsModel = mock(ShowPostModel.class);
		response = mock(HttpServletResponse.class);
		request = mock(HttpServletRequest.class);
		resourceList = new ListPosts(postsModel, request, response);
	}
	
	@Test
	public void shouldFindSinglePost() throws IOException {
		// Arrange
		PostEntity postEntity = new PostEntity(new UserEntity("u1", ""), 1, 1, "s1", "c1", null);
		@SuppressWarnings("unchecked") List<String> asList = Arrays.asList(new String[] {"tag1"});
		postEntity.setTags(asList);
		postEntity.setId(400);
		postEntity.setThreadId("1234");
		when(showPostsModel.findPostById(0)).thenReturn(postEntity);
		SinglePostRequest resourceShow = new SinglePostRequest(showPostsModel, request, response);
		
		// Act
		PostResource result = resourceShow.findById(0);
		
		// Assert
		assertEquals(1, result.getCreation());
		assertEquals(1, result.getModification());
		assertEquals("1234", result.getThreadId());
		assertEquals("u1", result.getUsername());
		assertEquals("s1", result.getSubject());
		assertEquals("c1", result.getContent());
		assertEquals("tag1", result.getTags().get(0));
		assertEquals(400, result.getId());
	}	
	
	@Test
	public void shouldListPosts() throws IOException {
		// Arrange
		List<PostEntity> posts = new ArrayList<PostEntity>();
		PostEntity postEntity = new PostEntity(new UserEntity("u1", ""), 1, 1, "s1", "c1", null);
		@SuppressWarnings("unchecked") List<String> asList = Arrays.asList(new String[] {"tag1"});
		postEntity.setTags(asList);
		postEntity.setId(400);
		posts.add(postEntity);
		posts.add(new PostEntity(new UserEntity("u2", ""), 2, 2, "s2", "c2", null));
		when(postsModel.listByModificationDate(0, 10)).thenReturn(posts);
		
		// Act
		ListPostsResource result = resourceList.listByModificationDate(0, 10);
		
		// Assert
		assertEquals(2, result.getPosts().size());
		assertEquals(400, result.getPosts().get(0).getId());
		assertEquals(1, result.getPosts().get(0).getCreation());
		assertEquals(1, result.getPosts().get(0).getModification());
		assertEquals("u1", result.getPosts().get(0).getUsername());
		assertEquals("s1", result.getPosts().get(0).getSubject());
		assertEquals("c1", result.getPosts().get(0).getContent());
		assertEquals("tag1", result.getPosts().get(0).getTags().get(0));
		assertEquals(2, result.getPosts().get(1).getCreation());
		assertEquals(2, result.getPosts().get(1).getModification());
		assertEquals("u2", result.getPosts().get(1).getUsername());
		assertEquals("s2", result.getPosts().get(1).getSubject());
		assertEquals("c2", result.getPosts().get(1).getContent());
	}
	
	@Test
	public void shouldListZeroPosts() throws IOException {
		// Arrange
		List<PostEntity> posts = new ArrayList<PostEntity>();
		when(postsModel.listByModificationDate(0, 10)).thenReturn(posts);
		
		// Act
		ListPostsResource result = resourceList.listByModificationDate(0, 10);
		
		// Assert
		assertEquals(0, result.getPosts().size());
	}
		
	@Test
	public void shouldListPostsWithThreadId() throws IOException {
		// Arrange
		List<PostEntity> posts = new ArrayList<PostEntity>();
		PostEntity postEntity = new PostEntity(new UserEntity("u1", ""), 1, 1, "s1", "c1", "threadId");
		postEntity.setId(400);
		posts.add(postEntity);
		when(postsModel.listByModificationDate(0, 10)).thenReturn(posts);
		
		// Act
		ListPostsResource result = resourceList.listByModificationDate(0, 10);
		
		// Assert
		assertEquals("threadId", result.getPosts().get(0).getThreadId());
	}	
	
	@Test
	public void shouldListPostsByThread() throws IOException {
		// Arrange
		List<PostEntity> posts = new ArrayList<PostEntity>();
		UserEntity ue = new UserEntity("u1", "");
		PostEntity postEntity = new PostEntity(ue, 1, 1, "s1", "c1", "t");
		postEntity.setId(400);
		posts.add(postEntity);
		posts.add(new PostEntity(new UserEntity("u2", ""), 2, 2, "s2", "c2", "t"));
		ThreadEntity thread = new ThreadEntity(postEntity, posts);
		thread.setNumPosts(5);
		ListThreadModel model = mock(ListThreadModel.class);
		when(model.listByThreadId("t", 0, 10)).thenReturn(posts);
		when(model.findThreadById("t")).thenReturn(thread);
		
		// Act
		ListThreadRequest res = new ListThreadRequest(model, request, response);
		ThreadResource result = res.listByThreadId("t", 0, 10);
		
		// Assert
		assertEquals(2, result.getPosts().size());
		assertEquals(5, result.getNumPosts());
		assertEquals(400, result.getPosts().get(0).getId());
		assertEquals(1, result.getPosts().get(0).getCreation());
		assertEquals(1, result.getPosts().get(0).getModification());
		assertEquals("s1", result.getSubject());
		assertEquals("u1", result.getAuthor());
		assertEquals("u1", result.getPosts().get(0).getUsername());
		assertEquals("s1", result.getPosts().get(0).getSubject());
		assertEquals("c1", result.getPosts().get(0).getContent());
		assertEquals(2, result.getPosts().get(1).getCreation());
		assertEquals(2, result.getPosts().get(1).getModification());
		assertEquals("u2", result.getPosts().get(1).getUsername());
		assertEquals("s2", result.getPosts().get(1).getSubject());
		assertEquals("c2", result.getPosts().get(1).getContent());
	}
	
}