package net.gvcc.goffice.multitenancy.exception;

public class MissingTenantException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1110660585088631727L;

	public MissingTenantException(String msg, Throwable e) {
		super(msg, e);
	}

	public MissingTenantException(String msg) {
		super(msg);
	}

}
