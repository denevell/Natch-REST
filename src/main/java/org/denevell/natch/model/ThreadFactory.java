package org.denevell.natch.model;

import java.util.Arrays;
import java.util.List;

  public class ThreadFactory {

    public ThreadEntity makeThread() {
      return null;
    }

    public ThreadEntity makeThread(PostEntity p) {
      ThreadEntity threadEntity = new ThreadEntity(p, Arrays.asList(p));
      threadEntity.setId(p.threadId);
      threadEntity.setNumPosts(threadEntity.getNumPosts() + 1);
      return threadEntity;
    }

    /**
     * Making a thread based on an existing thread
     */
    public ThreadEntity makeThread(ThreadEntity thread, PostEntity p) {
      p.subject = (thread.getRootPost().getSubject());
      thread.setLatestPost(p);
      List<PostEntity> posts = thread.getPosts();
      if (posts != null) {
        posts.add(p);
      } else {
        posts = Arrays.asList(p);
      }
      thread.setPosts(posts);
      thread.setNumPosts(thread.getNumPosts() + 1);
      return thread;
    }

  }