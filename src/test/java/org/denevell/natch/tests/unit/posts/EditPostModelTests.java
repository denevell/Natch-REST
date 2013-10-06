package org.denevell.natch.tests.unit.posts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.db.entities.UserEntity;
import org.denevell.natch.serv.posts.PostEntityAdapter;
import org.denevell.natch.serv.posts.PostsModel;
import org.denevell.natch.serv.posts.ThreadFactory;
import org.junit.Before;
import org.junit.Test;

public class EditPostModelTests {
	
	private PostsModel model;
	private EntityTransaction trans;
	private EntityManagerFactory factory;
	private EntityManager entityManager;
	private PostEntityAdapter postEntityAdapter;
	private ThreadFactory threadFactory;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		factory = mock(EntityManagerFactory.class);
		trans = mock(EntityTransaction.class);
		postEntityAdapter = mock(PostEntityAdapter.class);
		when(entityManager.getTransaction()).thenReturn(trans);
		threadFactory = mock(ThreadFactory.class);
		model = spy(new PostsModel(factory, entityManager, threadFactory));
	}
	
	@Test
	public void shouldEditPost() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity(null, 1, 1, "df", "sdf", null);
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		PostEntity postToBeEdited = new PostEntity(null, 1, 1, "xxx", "xxx", null);
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(postToBeEdited);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.EDITED, result);
	}
	
	@Test
	public void shouldReturnUnAuthorised() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("that_person");
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(post);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.NOT_YOURS_TO_DELETE, result);
	}
	
	@Test
	public void shouldReturnNotFound() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num+1);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("that_person");
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(post);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.DOESNT_EXIST, result);
	}
	
	@Test
	public void shouldReturnUnknownErrorOnException() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doThrow(new RuntimeException()).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(post);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.UNKNOWN_ERROR, result);
	}
	
	@Test
	public void shouldReturnUnknownErorrOnNullUser() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(post);
		
		// Act
		String result = model.edit(null, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.UNKNOWN_ERROR, result);
	}
	
	@Test
	public void shouldReturnUnknownErorrOnNullPost() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		
		// Act
		String result = model.edit(userEntity, num, null, false);
		
		// Assert 
		assertEquals(PostsModel.UNKNOWN_ERROR, result);
	}
	
	@Test
	public void shouldReturnBadUserInputOnBlanks() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		PostEntity postToEdit = new PostEntity(null, 1, 1, " ", " ", null);
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(postToEdit);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.BAD_USER_INPUT, result);
	}
	
	@Test
	public void shouldReturnBadUserInputOnNull() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		PostEntity postToEdit = new PostEntity(null, 1, 1, null, null, null);
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(postToEdit);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, true);
		
		// Assert 
		assertEquals(PostsModel.BAD_USER_INPUT, result);
	}
	
	@Test
	public void shouldReturnBadUserInputOnBlankSubject() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		PostEntity postToEdit = new PostEntity(null, 1, 1, " ", "sdfsdf", null);
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(postToEdit);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, true);
		
		// Assert 
		assertEquals(PostsModel.BAD_USER_INPUT, result);
	}
	
	@Test
	public void shouldReturnUnknownErorrOnNullFromPostAdpater() {
		// Arrange
		long num = 1;
		PostEntity post = new PostEntity();
		post.setUser(new UserEntity("this_person", null));
		doReturn(post).when(model).findPostById(num);
		UserEntity userEntity = new UserEntity();
		userEntity.setUsername("this_person");
		when(postEntityAdapter.createPost(post, userEntity)).thenReturn(null);
		
		// Act
		String result = model.edit(userEntity, num, postEntityAdapter, false);
		
		// Assert 
		assertEquals(PostsModel.UNKNOWN_ERROR, result);
	}
}