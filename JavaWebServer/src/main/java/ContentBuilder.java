package main.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentBuilder {

	public static enum CONTENTTYPE {
		OK, BAD_REQUEST, NOT_FOUND, UNAUTHORISED
	};

	private final static Logger LOGGER = LoggerFactory.getLogger(ContentBuilder.class);

	private static void logInfo(String message) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(message);
	}

	public static String buildContent(CONTENTTYPE type, Object... arguments) {
		switch (type) {
			case OK: {
				if (arguments.length < 1) {
					logInfo("NO_CONTENT CONTENT SENT");
					return buildNoContentContent();
				}
				logInfo("NOT_FOUND CONTENT SENT");
				return buildContent((String) arguments[0]);
			}
			case BAD_REQUEST: {
				logInfo("BAD_REQUEST CONTENT SENT");
				return buildBadRequestContent();
			}
			case NOT_FOUND: {
				logInfo("NOT_FOUND CONTENT SENT");
				return buildNotFoundContent();
			}
			case UNAUTHORISED: {
				logInfo("UNAUTHORISED CONTENT SENT");
				return buildUnauthorisedContent();
			}
		}
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("WHAT HOW?");
		}
		return "YOU SHOULD NEVER GET HERE";
	}

	private static String buildNoContentContent() {
		return "NO_CONTENT";
	}

	private static String buildContent(String html) {
		return html;
	}

	private static String buildBadRequestContent() {
		return "BAD_REQUEST";
	}

	private static String buildNotFoundContent() {
		return "NOT_FOUND";
	}

	private static String buildUnauthorisedContent() {
		return "UNAUTHORISED";
	}
}
