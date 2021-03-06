package org.denevell.natch.model.impl;

import java.util.List;

import org.denevell.jrappy.Jrappy;
import org.denevell.natch.model.entities.PostEntity;
import org.denevell.natch.model.entities.ThreadEntity;
import org.denevell.natch.model.interfaces.ThreadListModel;
import org.denevell.natch.utils.JPAFactoryContextListener;
import org.jvnet.hk2.annotations.Service;

@Service
public class ThreadListModelImpl implements ThreadListModel {

  private Jrappy<PostEntity> mPostModel = new Jrappy<PostEntity>(
      JPAFactoryContextListener.sFactory);
  private Jrappy<ThreadEntity> mThreadModel = new Jrappy<ThreadEntity>(
      JPAFactoryContextListener.sFactory);

  @Override
  public ThreadAndPosts list(String id, int start, int maxNumPosts) {
    try {
      List<PostEntity> posts = null;
      ThreadEntity thread = null;
      posts = mPostModel.startTransaction().start(start).max(maxNumPosts)
          .namedQuery(PostEntity.NAMED_QUERY_FIND_BY_THREADID)
          .queryParam("threadId", id).list(PostEntity.class);
      if (posts != null) {
        thread = mThreadModel.useTransaction(mPostModel.getEntityManager())
            .namedQuery(ThreadEntity.NAMED_QUERY_FIND_THREAD_BY_ID)
            .queryParam("id", id).single(ThreadEntity.class);
      }
      if (posts == null || thread == null || posts.size() == 0) {
        return null;
      } else {
        return new ThreadAndPosts(thread, posts);
      }

    } finally {
      mPostModel.commitAndCloseEntityManager();

    }
  }

}
