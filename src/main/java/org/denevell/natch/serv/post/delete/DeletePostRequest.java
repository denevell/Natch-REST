package org.denevell.natch.serv.post.delete;

import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.denevell.natch.auth.LoginHeadersFilter;
import org.denevell.natch.db.entities.UserEntity;
import org.denevell.natch.io.posts.DeletePostResourceReturnData;
import org.denevell.natch.utils.Log;
import org.denevell.natch.utils.Strings;

import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("post/del")
public class DeletePostRequest {
	
	public final static String EDITED = "edited";
	public final static String DELETED = "deleted";
	public final static String ADDED = "added";
	public final static String DOESNT_EXIST = "doesntexist";
	public final static String UNKNOWN_ERROR = "unknownerror";
	public final static String BAD_USER_INPUT = "baduserinput";
	public final static String NOT_YOURS_TO_DELETE = "notyourtodelete";	
	@Context UriInfo mInfo;
	@Context HttpServletRequest mRequest;
	@Context ServletContext context;
	@Context HttpServletResponse mResponse;
	private DeletePostModel mModel;
	private ResourceBundle rb = Strings.getMainResourceBundle();
	
	public DeletePostRequest() {
		mModel = new DeletePostModel();
	}
	
	/**
	 * For DI testing.
	 */
	public DeletePostRequest(DeletePostModel postModel, 
			HttpServletRequest request, 
			HttpServletResponse response
			) {
		mModel = postModel;
		mRequest = request;
		mResponse = response;
	}
		
	@DELETE
	@Path("{postId}") // Explicit for the servlet filter
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete a post", 	notes="Must contain the AuthKey header.",
		responseClass="org.denevell.natch.serv.posts.resources.DeletePostResourceReturnData")
	@ApiErrors({
		@ApiError(code=401, reason="Incorrect AuthKey header.")
	})	
	public DeletePostResourceReturnData delete(
			@ApiParam(name="postId") @PathParam("postId") long number) {
		DeletePostResourceReturnData ret = new DeletePostResourceReturnData();
		ret.setSuccessful(false);
		UserEntity userEntity = LoginHeadersFilter.getLoggedInUser(mRequest);
		try {
			mModel.init();
			if(userEntity==null) {
				ret.setError(rb.getString(Strings.unknown_error)); // Unknown as this shouldn't happen
				return ret;
			}	
			String result = mModel.delete(userEntity, number);
			generateDeleteReturnResource(result, ret, userEntity);
			return ret;
		} catch(Exception e) {
			Log.info(getClass(), "Couldn't delete post: " + e.toString());
			ret.setError(rb.getString(Strings.unknown_error));
			return ret;
		} finally {
			mModel.close();
		} 
	}

	private void generateDeleteReturnResource(String result, DeletePostResourceReturnData ret, UserEntity userEntity) {
		if(result.equals(DELETED)) {
			ret.setSuccessful(true);
		} else if(result.equals(DOESNT_EXIST)) {
			ret.setError(rb.getString(Strings.post_doesnt_exist));
		} else if(result.equals(NOT_YOURS_TO_DELETE)) {
			ret.setError(rb.getString(Strings.post_not_yours));
		} else if(result.equals(UNKNOWN_ERROR)) {
			ret.setError(rb.getString(Strings.unknown_error));
		}
	}

}
