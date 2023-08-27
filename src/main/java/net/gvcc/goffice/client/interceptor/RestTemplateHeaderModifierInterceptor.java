/*
 * goffice... 
 * https://www.goffice.org
 * 
 * Copyright (c) 2005-2022 Consorzio dei Comuni della Provincia di Bolzano Soc. Coop. <https://www.gvcc.net>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.gvcc.goffice.client.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.gvcc.goffice.Constants;
import net.gvcc.goffice.language.ILanguageStorage;
import net.gvcc.goffice.multitenancy.ITenantService;
import net.gvcc.goffice.opentracing.IOpenTracingStorage;

/**
 * @author marco.mancuso
 *
 */
@Import({ ITenantService.class, ILanguageStorage.class, IOpenTracingStorage.class })
public class RestTemplateHeaderModifierInterceptor implements ClientHttpRequestInterceptor {
	private static Logger LOGGER = LoggerFactory.getLogger(RestTemplateHeaderModifierInterceptor.class);

	@SuppressFBWarnings(value = { "MS_EXPOSE_REP", "EI_EXPOSE_REP", "EI_EXPOSE_REP2" }, justification = "avoid clone objects")
	ITenantService tenantService;

	@SuppressFBWarnings(value = { "MS_EXPOSE_REP", "EI_EXPOSE_REP", "EI_EXPOSE_REP2" }, justification = "avoid clone objects")
	ILanguageStorage languageStorage;

	@SuppressFBWarnings(value = { "MS_EXPOSE_REP", "EI_EXPOSE_REP", "EI_EXPOSE_REP2" }, justification = "avoid clone objects")
	IOpenTracingStorage openTracingStorage;

	public RestTemplateHeaderModifierInterceptor(ITenantService tenantService, ILanguageStorage languageStorage, IOpenTracingStorage openTracingStorage) {
		super();
		this.tenantService = tenantService;
		this.languageStorage = languageStorage;
		this.openTracingStorage = openTracingStorage;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		LOGGER.trace("intercept - START");

		HttpHeaders headers = request.getHeaders();

		String headerValue = tenantService.getTenant();
		LOGGER.trace("intercept - added tenant header: {}={}", Constants.TENANT_HEADER_NAME, headerValue);
		headers.add(Constants.TENANT_HEADER_NAME, headerValue);

		headerValue = languageStorage.getLanguage();
		headers.add(Constants.LANGUAGE_HEADER_NAME, headerValue);
		LOGGER.trace("intercept - added language header: {}={}", Constants.LANGUAGE_HEADER_NAME, headerValue);

		Map<String, List<String>> openTracingHeaders = openTracingStorage.getHeaders();
		if (openTracingHeaders != null) {
			openTracingHeaders.keySet().forEach(headerName -> {
				List<String> headerValues = openTracingHeaders.get(headerName);
				if (headerValues != null) {
					headers.addAll(headerName, headerValues);
					LOGGER.trace("intercept - added opentracing headers: {}={}", headerName, headerValues.toString());
				}
			});
		}

		ClientHttpResponse response = execution.execute(request, body);

		LOGGER.trace("intercept - END");

		return response;
	}
}