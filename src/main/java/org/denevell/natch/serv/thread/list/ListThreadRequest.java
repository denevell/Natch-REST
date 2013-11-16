package org.denevell.natch.serv.thread.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.denevell.natch.db.entities.PostEntity;
import org.denevell.natch.db.entities.ThreadEntity;
import org.denevell.natch.io.posts.PostResource;
import org.denevell.natch.io.threads.ThreadResource;

@Path("post/thread")
public class ListThreadRequest {
	
	@Context UriInfo mInfo;
	@Context HttpServletRequest mRequest;
	@Context ServletContext context;
	@Context HttpServletResponse mResponse;
	private ListThreadModel mModel;
	
	public ListThreadRequest() {
		mModel = new ListThreadModel();
	}
	
	/**
	 * For DI testing.
	 * @param editPostAdapter 
	 */
	public ListThreadRequest(ListThreadModel postModel, HttpServletRequest request, HttpServletResponse response) {
		mModel = postModel;
		mRequest = request;
		mResponse = response;
	}
		
	@GET
	@Path("/{threadId}/{start}/{limit}")
	@Produces(MediaType.APPLICATION_JSON)	
	public ThreadResource listByThreadId(
			@PathParam("threadId") String threadId,
			@PathParam("start") int start, 	
			@PathParam("limit") int limit) throws IOException {
		List<PostEntity> posts = null;
		ThreadEntity thread= null;
		String username = null;
		List<String> tags = null;
		try {
			mModel.init();
			posts = mModel.listByThreadId(threadId, start, limit);
			if(posts!=null) thread = mModel.findThreadById(threadId);
			if(thread!=null) username = thread.getRootPost().getUser().getUsername();
			if(thread!=null) tags= thread.getRootPost().getTags();
			if(thread==null) {
				mResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				return null;
			}
		} finally {
			mModel.close();
		}
		if(posts!=null && posts.size()==0) {
			mResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		} else {
			ThreadResource adaptedPosts = adaptPosts(
					username, posts, thread);
			adaptedPosts.setTags(tags);
			return adaptedPosts;
		}
	}

	private ThreadResource adaptPosts(String threadAuthor, List<PostEntity> posts, ThreadEntity thread) {
		ThreadResource tr =  new ThreadResource();
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
		tr.setSubject(thread.getRootPost().getSubject());
		tr.setAuthor(thread.getRootPost().getUser().getUsername());
		tr.setPosts(postsResources);
		tr.setNumPosts((int) thread.getNumPosts());
		return tr;
	}	

}