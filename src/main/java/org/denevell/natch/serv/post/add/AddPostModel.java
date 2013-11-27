package org.denevell.natch.serv.post.add;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;

import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.db.entities.ThreadEntity;
import org.denevell.natch.db.entities.UserEntity;
import org.denevell.natch.db.entities.UserEntityQueries;
import org.denevell.natch.io.posts.AddPostResourceInput;
import org.denevell.natch.serv.thread.add.ThreadFactory;
import org.denevell.natch.utils.EntityUtils;
import org.denevell.natch.utils.JPAFactoryContextListener;
import org.denevell.natch.utils.Log;

public class AddPostModel {

	private EntityManager mEntityManager;
	private ThreadFactory mThreadFactory;
    private UserEntityQueries mUserEntityQueries;
	
	public AddPostModel() {
		mThreadFactory = new ThreadFactory();
		mUserEntityQueries = new UserEntityQueries(null);
	}
	
	public void init() {
		EntityManagerFactory factory = JPAFactoryContextListener.sFactory;
		mEntityManager = factory.createEntityManager(); 
	}
	
	public void close() {
		EntityUtils.closeEntityConnection(mEntityManager); 
	}
	
	/**
	 * For testing / di
	 */
	public AddPostModel(EntityManagerFactory factory, 
	        EntityManager entityManager, 
	        ThreadFactory threadFactory, 
	        UserEntityQueries userQueries) {
		mEntityManager = entityManager;
		mThreadFactory = threadFactory;
		mUserEntityQueries = userQueries;
	}

	public ThreadEntity addPostAsDifferntUser(String userId, AddPostResourceInput input) {
	    List<UserEntity> user = mUserEntityQueries.getUserByUsername(userId, mEntityManager);
	    return addPost(user.get(0), input, true);
	}

	public ThreadEntity addPost(UserEntity user, AddPostResourceInput input) {
	    return addPost(user, input, false);
	}
	
	public ThreadEntity addPost(UserEntity user, AddPostResourceInput input, boolean adminEdited) {
		EntityTransaction trans = null;
		try {
			long created = new Date().getTime();
			String threadId = null;
			if(input.getThreadId()==null || input.getThreadId().isEmpty()) {
				threadId = getThreadId(input.getSubject(), input.getThreadId(), created);
			} else {
				threadId = input.getThreadId();
			}
			PostEntity mPost = new PostEntity();
			mPost.setContent(input.getContent());
			mPost.setSubject(input.getSubject());
			mPost.setThreadId(threadId);
			mPost.setTags(input.getTags());			
			mPost.setUser(user);
			mPost.setCreated(created);
			mPost.setModified(created);			
			if(adminEdited) {
			    mPost.adminEdited();
			}
			trans = mEntityManager.getTransaction();
			trans.begin();
			ThreadEntity thread = findThreadById(mPost.getThreadId());
			if(thread==null) {
				thread = mThreadFactory.makeThread(mPost);
			} else {
				thread = mThreadFactory.makeThread(thread, mPost);
			}
			mEntityManager.persist(thread);
			trans.commit();
			return thread;
		} catch(Exception e) {
			Log.info(this.getClass(), e.toString());
			e.printStackTrace();
			if(trans!=null && trans.isActive()) trans.rollback();
			return null;
		} 
	}
	
	private String getThreadId(String subject, String threadId, long time) {
		if(threadId==null || threadId.trim().length()==0) {
			try {
				MessageDigest md5Algor = MessageDigest.getInstance("MD5");
				StringBuffer sb = new StringBuffer();
				byte[] digest = md5Algor.digest(subject.getBytes());
				for (byte b : digest) {
					sb.append(Integer.toHexString((int) (b & 0xff)));
				}				
				threadId = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				Log.info(getClass(), "Couldn't get an MD5 hash. I guess we'll just use hashCode() then.");
				e.printStackTrace();
				threadId = String.valueOf(subject.hashCode());
			}
			threadId = threadId+String.valueOf(time);
		}
		return threadId;
	}		
	
	public ThreadEntity findThreadById(String id) {
		try {
		    ThreadEntity thread = mEntityManager.find(ThreadEntity.class, id, LockModeType.PESSIMISTIC_READ);
			return thread;
		} catch(Exception e) {
			Log.info(getClass(), "Error finding thread by id: " + e.toString());
			return null;
		} 
	}		

}