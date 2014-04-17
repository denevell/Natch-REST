package org.denevell.natch.adapters;

import java.util.Date;

import org.denevell.natch.io.posts.AddPostResourceInput;
import org.denevell.natch.model.entities.PostEntity;
import org.denevell.natch.model.entities.UserEntity;

public class AddPostRequestToPostEntity {

	public static PostEntity adapt(
			AddPostResourceInput input,
			boolean adminEdited,
			UserEntity userEntity) {
		PostEntity pe = new PostEntity();
		long created = new Date().getTime();
		pe.setContent(input.getContent());
		pe.setSubject(input.getSubject());
		pe.setThreadId(input.getThreadId());
		pe.setTags(input.getTags());
		pe.setUsername(userEntity.getUsername());
		pe.setCreated(created);
		pe.setModified(created);
		if (adminEdited) {
			pe.adminEdited();
		}
		return pe;
	}

}
