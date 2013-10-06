package org.denevell.natch.serv.addpost;

import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.denevell.natch.auth.LoginHeadersFilter;
import org.denevell.natch.db.entities.UserEntity;
import org.denevell.natch.io.posts.AddPostResourceInput;
import org.denevell.natch.io.posts.AddPostResourceReturnData;
import org.denevell.natch.serv.posts.AddPostResourcePostEntityAdapter;
import org.denevell.natch.serv.posts.PostsModel;
import org.denevell.natch.utils.Log;
import org.denevell.natch.utils.Strings;

import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("post/add")
public class AddPostRequest {
	
	@Context UriInfo mInfo;
	@Context HttpServletRequest mRequest;
	@Context ServletContext context;
	@Context HttpServletResponse mResponse;
	private PostsModel mModel;
	private AddPostResourcePostEntityAdapter mAddPostAdapter;
	private ResourceBundle rb = Strings.getMainResourceBundle();
	
	public AddPostRequest() {
		mModel = new PostsModel();
		mAddPostAdapter = new AddPostResourcePostEntityAdapter();
	}
	
	/**
	 * For DI testing.
	 */
	public AddPostRequest(PostsModel postModel, 
			HttpServletRequest request, 
			HttpServletResponse response,
			AddPostResourcePostEntityAdapter adapter) {
		mModel = postModel;
		mRequest = request;
		mResponse = response;
		mAddPostAdapter = adapter;
	}
		
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add a post",	notes="Must contain the AuthKey header.",
		responseClass="org.denevell.natch.serv.posts.resources.AddPostResourceReturnData")
	@ApiErrors({
		@ApiError(code=401, reason="Incorrect AuthKey header.")
	})	
	public AddPostResourceReturnData addPost(AddPostResourceInput input) {
		UserEntity userEntity = LoginHeadersFilter.getLoggedInUser(mRequest);
		if(PostsModel.isBadInputParams(userEntity, 
				input.getSubject(), 
				input.getContent(), false)) {
			AddPostResourceReturnData regReturnData = new AddPostResourceReturnData();
			regReturnData.setSuccessful(false);
			regReturnData.setError(rb.getString(Strings.post_fields_cannot_be_blank));
			return regReturnData;
		} else {
			return addPost(input, userEntity);
		}
	}
	
	private AddPostResourceReturnData addPost(AddPostResourceInput input, UserEntity userEntity) {
		try {
			mModel.init();
			AddPostResourceReturnData regReturnData = new AddPostResourceReturnData();
			regReturnData.setSuccessful(false);
			mAddPostAdapter.create(input);
			String okay = mModel.addPost(userEntity, mAddPostAdapter);
			generateAddPostReturnResource(regReturnData, okay, mAddPostAdapter);
			return regReturnData;
		} finally {
			mModel.close();
		}
	}	
	
	private void generateAddPostReturnResource(AddPostResourceReturnData regReturnData, String okay, AddPostResourcePostEntityAdapter adapterThatCreatePost) {
		if(okay.equals(PostsModel.ADDED)) {
			if(adapterThatCreatePost!=null && adapterThatCreatePost.getCreatedPost()!=null) {
				regReturnData.setThreadId(adapterThatCreatePost.getCreatedPost().getThreadId());
			} else {
				Log.info(getClass(), "Added a post but the thread id was null when sending the json response...");
			}
			regReturnData.setSuccessful(true);
		} else if(okay.equals(PostsModel.BAD_USER_INPUT)) {
			regReturnData.setSuccessful(false);
			regReturnData.setError(rb.getString(Strings.post_fields_cannot_be_blank));
		} else if(okay.equals(PostsModel.UNKNOWN_ERROR)){
			regReturnData.setSuccessful(false);
			regReturnData.setError(rb.getString(Strings.unknown_error));
		} else {
			regReturnData.setSuccessful(false);
			regReturnData.setError(rb.getString(Strings.unknown_error));
		}
	}	

}
