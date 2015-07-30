package org.springframework.restdocs.hypermedia;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StringUtils;

/**
 * {@link LinkExtractor} that delegates to other link extractors based on the response's
 * content type.
 * 
 * @author Andy Wilkinson
 *
 */
class ContentTypeLinkExtractor implements LinkExtractor {

	private Map<MediaType, LinkExtractor> linkExtractors = new HashMap<>();

	ContentTypeLinkExtractor() {
		this.linkExtractors.put(MediaType.APPLICATION_JSON, new AtomLinkExtractor());
		this.linkExtractors.put(HalLinkExtractor.HAL_MEDIA_TYPE, new HalLinkExtractor());
	}

	ContentTypeLinkExtractor(Map<MediaType, LinkExtractor> linkExtractors) {
		this.linkExtractors.putAll(linkExtractors);
	}

	@Override
	public Map<String, List<Link>> extractLinks(MockHttpServletResponse response)
			throws IOException {
		String contentType = response.getContentType();
		LinkExtractor extractorForContentType = getExtractorForContentType(contentType);
		if (extractorForContentType != null) {
			return extractorForContentType.extractLinks(response);
		}
		throw new IllegalStateException(
				"No LinkExtractor has been provided and one is not available for the "
						+ "content type " + contentType);
	}

	private LinkExtractor getExtractorForContentType(String contentType) {
		if (StringUtils.hasText(contentType)) {
			MediaType mediaType = MediaType.parseMediaType(contentType);
			for (Entry<MediaType, LinkExtractor> entry : this.linkExtractors.entrySet()) {
				if (mediaType.isCompatibleWith(entry.getKey())) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

}