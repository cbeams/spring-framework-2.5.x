package org.springframework.web.multipart;

/**
 * MultipartException subclass thrown when an upload exceeds the
 * maximum allowed size.
 * @author Juergen Hoeller
 * @since 31.03.2004
 */
public class MaxUploadSizeExceededException extends MultipartException {

	private long maxUploadSize;

	public MaxUploadSizeExceededException(String msg) {
		super(msg);
	}

	public MaxUploadSizeExceededException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public MaxUploadSizeExceededException(long maxUploadSize) {
		this(maxUploadSize, null);
	}

	public MaxUploadSizeExceededException(long maxUploadSize, Throwable ex) {
		super("Maximum upload size of " + maxUploadSize + " bytes exceeded", ex);
		this.maxUploadSize = maxUploadSize;
	}

	public long getMaxUploadSize() {
		return maxUploadSize;
	}

}
