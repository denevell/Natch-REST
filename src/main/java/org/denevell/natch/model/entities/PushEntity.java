package org.denevell.natch.model.entities;

public class PushEntity {
	
	public static String NAMED_QUERY_FIND_ID = "findId";
	public static String NAMED_QUERY_LIST_IDS = "listIds";
	
	private String clientId;
	
	public PushEntity() {
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}
