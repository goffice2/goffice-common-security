package net.gvcc.goffice.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.gvcc.goffice.language.interceptor.LanguageInterceptor;
import net.gvcc.goffice.logger.headers.interceptor.RequestHeadersLoggingInterceptor;
import net.gvcc.goffice.multitenancy.interceptor.MultiTenantInterceptor;
import net.gvcc.goffice.opentracing.interceptor.OpenTracingInterceptor;

/**
 * @author marco.mancuso
 *
 */
@Import({ MultiTenantInterceptor.class, LanguageInterceptor.class, OpenTracingInterceptor.class, RequestHeadersLoggingInterceptor.class })
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	private static final Logger LOGGER = LogManager.getLogger(WebConfiguration.class);

	@Autowired
	MultiTenantInterceptor multiTenantInterceptor;

	@Autowired
	LanguageInterceptor languageInterceptor;

	@Autowired
	OpenTracingInterceptor openTracingInterceptor;

	@Autowired
	RequestHeadersLoggingInterceptor requestHeadersLoggingInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		LOGGER.info("addInterceptors - START");

		LOGGER.debug("addInterceptors - adding tenant interceptor...");
		registry.addInterceptor(multiTenantInterceptor);

		LOGGER.debug("addInterceptors - adding language interceptor...");
		registry.addInterceptor(languageInterceptor);

		LOGGER.debug("addInterceptors - adding open tracing interceptor...");
		registry.addInterceptor(openTracingInterceptor);

		LOGGER.debug("addInterceptors - adding request headers interceptor...");
		registry.addInterceptor(requestHeadersLoggingInterceptor);

		LOGGER.info("addInterceptors - END");
	}
}
