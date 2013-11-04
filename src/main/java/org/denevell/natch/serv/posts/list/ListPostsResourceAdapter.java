package org.denevell.natch.serv.posts.list;

import java.util.ArrayList;
import java.util.List;

import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.io.posts.ListPostsResource;
import org.denevell.natch.io.posts.PostResource;

public class ListPostsResourceAdapter extends ListPostsResource {

	public ListPostsResourceAdapter(List<PostEntity> posts) {
		List<PostResource> postsResources = new ArrayList<PostResource>();
		for (PostEntity p: posts) {
			PostResource postResource = new PostResource(p.getUser().getUsername(), 
					p.getCreated(), 
					p.getModified(), 
					p.getSubject(), 
					p.getContent(),
					p.getTags(), 
					p.isAdminEdited());
			postResource.setId(p.getId());
			postResource.setThreadId(p.getThreadId());
			postsResources.add(postResource);
		}
		setPosts(postsResources);
	}

}
