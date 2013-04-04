package org.denevell.natch.serv.posts;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.denevell.natch.db.entities.PersistenceInfo;
import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.utils.EntityUtils;
import org.denevell.natch.utils.Log;

public class PostsModel {

	public enum AddPostResult {
		ADDED, UNKNOWN_ERROR, BAD_USER_INPUT
	}

	private EntityManagerFactory mFactory;
	private EntityManager mEntityManager;
	private PostFactory mPostFactory;

	public PostsModel() {
		mFactory = Persistence.createEntityManagerFactory(PersistenceInfo.EntityManagerFactoryName);
		mEntityManager = mFactory.createEntityManager();		
		mPostFactory = new PostFactory();
	}
	
	/**
	 * For testing / di
	 */
	public PostsModel(EntityManagerFactory factory, EntityManager entityManager, PostFactory postFactory) {
		mFactory = factory;
		mEntityManager = entityManager;
		mPostFactory = postFactory;
	}
	
	public AddPostResult addPost(String subject, String content) {
		PostEntity p = mPostFactory.createPost(subject, content);
		if(checkInputParams(subject, content) || p ==null) {
			return AddPostResult.BAD_USER_INPUT;
		}
		EntityTransaction trans = null;
		try {
			trans = mEntityManager.getTransaction();
			trans.begin();
			mEntityManager.persist(p);
			trans.commit();
			return AddPostResult.ADDED;
		} catch(Exception e) {
			Log.info(this.getClass(), e.toString());
			e.printStackTrace();
			if(trans!=null && trans.isActive()) trans.rollback();
			return AddPostResult.UNKNOWN_ERROR;
		} finally {
			EntityUtils.closeEntityConnection(mFactory, mEntityManager);		
		}		
	}

	private boolean checkInputParams(String subject, String content) {
		return subject==null || content==null || subject.trim().length()==0 || content.trim().length()==0;
	}


}