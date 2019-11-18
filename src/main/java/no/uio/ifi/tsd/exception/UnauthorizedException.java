package no.uio.ifi.tsd.exception;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.fasterxml.jackson.core.JsonToken;

public class UnauthorizedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8215211033759084971L;

	public UnauthorizedException() {
		super( new JSONObject().put("message", "Authentication failed").toString());
	}

}
