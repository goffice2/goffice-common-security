package net.gvcc.goffice.multitenancy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.gvcc.goffice.modelmapper.ModelMapperHelper;
import net.gvcc.goffice.multitenancy.exception.MissingTenantException;

@Component
public class TenantConfigurator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TenantConfigurator.class);

	private static final String UPDATE_FILE_SUFFIX = ".updates";

	public interface ITenantConfiguratorListener {

		void onReloading(TenantMap tenantMap);

		void onReloaded(TenantMap tenantMap);

		void onReloadingError();
	}

	private interface ITenantFilter {
		String getValue(TenantInfo tenantInfo);

		String getLabel();
	}

	private static enum ConfigurationEvent {
		RELOADING, //
		RELOADED, //
		RELOADIN_ERROR
	}

	/////////////////////////////////////////////////////////////////////////////

	private TenantMap tenantMap;

	@Value("${goffice.common.persistence.tenant.file}")
	private String tenantFileName;

	@Autowired
	private ITenantStorage tenantStorage;

	private static long lastReloading = -1L;
	private static String lastConfigMd5 = "";

	private List<ITenantConfiguratorListener> listeners = new ArrayList<>();

	private static DateFormat SDF_BACKUP = new SimpleDateFormat("'.'yyyymmdd_hhMMss'.backup'");
	private static DateFormat SDF_ERROR = new SimpleDateFormat("'.'yyyymmdd_hhMMss'.error'");

	/////////////////////////////////////////////////////////////////////////////

	@PostConstruct
	public synchronized void initTenants() {
		LOGGER.debug("initTenants - START");

		prepareUpdateFileIfAvailable();
		loadTenants();

		LOGGER.debug("initTenants - END");
	}

	private synchronized void loadTenants() {
		LOGGER.debug("loadTenants - START");

		lastReloading = -1;
		lastConfigMd5 = "";

		ObjectMapper mapper = new ObjectMapper();

		File file = null;

		try {
			LOGGER.info("Reading cmdb info from: {}", tenantFileName);
			file = ResourceUtils.getFile(tenantFileName);

			// executing the listeners before updating data
			noytifyConfigurationEventToListeners(ConfigurationEvent.RELOADING);

			if (tenantMap != null) {
				tenantMap.clear(); // free memory
			}
			tenantMap = mapper.readValue(file, TenantMap.class);

			lastReloading = file.lastModified();
			lastConfigMd5 = getMD5(file);
			LOGGER.info("Reading cmdb...DONE");

			if (LOGGER.isDebugEnabled() && tenantMap.getTenants() != null) {
				tenantMap.getTenants().forEach((key, tenantInfo) -> {
					LOGGER.debug("*** tenant: {} ***", key);
					LOGGER.debug(tenantInfo.toString());
				});
			}

			// executing the listeners after updating data
			noytifyConfigurationEventToListeners(ConfigurationEvent.RELOADED);
		} catch (IOException e) {
			LOGGER.error("Unable to load tenant file: {} (absolute path:  {})", tenantFileName, file == null ? "[NULL]" : file.getAbsolutePath(), e);

			// executing the listeners before updating data
			noytifyConfigurationEventToListeners(ConfigurationEvent.RELOADIN_ERROR);
		}

		LOGGER.debug("loadTenants - END");
	}

	private String getFilenameForUpdates() {
		return tenantFileName.concat(UPDATE_FILE_SUFFIX);
	}

	private void prepareUpdateFileIfAvailable() {
		LOGGER.debug("checkIfUpdatesAraAvailables - START");

		if (isAClasspathConfiguration()) {
			LOGGER.trace("prepareUpdateFileIfAvailable - the tenant configuration has a classpath file reference: is a fix configuration and it is not possibile to update!");
		} else {
			File newFile = new File(getFilenameForUpdates());
			if (newFile.exists()) {
				File elaboratingFile = new File(newFile.getParent(), newFile.getName().concat(".elaborating"));

				if (elaboratingFile.exists()) {
					LOGGER.warn("prepareUpdateFileIfAvailable - ***** UPDATING ALREADY IN PROGRESS ****");
				} else {
					boolean error = true;

					try {
						File targetFile = new File(tenantFileName);
						File backupFile = new File(tenantFileName.concat(SDF_BACKUP.format(new Date())));

						newFile.renameTo(elaboratingFile);

						// test config
						new ObjectMapper().readValue(elaboratingFile, TenantMap.class);

						FileUtils.copyFile(targetFile, backupFile);

						FileUtils.copyFile(elaboratingFile, targetFile);

						elaboratingFile.delete();

						error = false;
					} catch (IOException e) {
						LOGGER.error("prepareUpdateFileIfAvailable", e);
					} finally {
						if (elaboratingFile.exists()) {
							File errorFile = new File(newFile.getParentFile(), newFile.getName().concat(SDF_ERROR.format(new Date())));
							elaboratingFile.renameTo(errorFile);
						}

						if (error) {
							LOGGER.error("*************************************************");
							LOGGER.error("**** PROBLEMS TO LOAD NEW CMDB CONFIGURATION ****");
							LOGGER.error("**** TRYING TO RESTORE THE LAST BACKUP FILE  ****");
							LOGGER.error("*************************************************");
						}
					}
				}
			}
		}

		LOGGER.debug("checkIfUpdatesAraAvailables - END");
	}

	protected synchronized boolean update(String jsonConfig) throws IOException {
		LOGGER.info("update - START");

		boolean updated = false;

		try {
			if (isAClasspathConfiguration()) {
				final String msg = String.format("Unable to perform update: the config has a classpath reference (%s), not a filesystem ref!", tenantFileName);
				throw new IOException(msg);
			}

			final Charset charset = Charset.defaultCharset();

			String newMD5 = DigestUtils.md5Hex(jsonConfig.getBytes(charset));
			if (lastConfigMd5.equals(newMD5)) { // if no modification occurred
				LOGGER.info("update - the new configuration is the same as the one currently loaded!");
			} else {
				String outputFileName = getFilenameForUpdates();
				File outputFile = new File(outputFileName);

				final String msg = "update - writing new CMDB configuration to file: {}...";
				LOGGER.info(msg, outputFile.getAbsolutePath());
				FileUtils.writeStringToFile(outputFile, jsonConfig, charset);
				LOGGER.info(msg.concat("DONE"), outputFile.getAbsoluteFile());

				updated = true;

				LOGGER.info("force reloading....");
				reloadTenantsIfRequired();
				LOGGER.info("force reloading....DONE");
			}
		} catch (IOException e) {
			LOGGER.error("update", e);
			throw e;
		}

		LOGGER.info("update - updated={}", updated);
		LOGGER.info("update - END");

		return updated;
	}

	private boolean isAClasspathConfiguration() {
		return tenantFileName.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX);
	}

	private synchronized boolean needsToBeReloaded() throws FileNotFoundException {
		LOGGER.debug("reloadRequired - START");

		prepareUpdateFileIfAvailable();

		File file = ResourceUtils.getFile(tenantFileName);
		boolean reload = lastReloading != file.lastModified();
		if (reload) {
			LOGGER.info("the filetime is different from the last read: going to check the MD5 signature...");

			String newMd5 = getMD5(file);
			reload = !lastConfigMd5.equals(newMd5);

			final String text = reload ? "" : "not ";
			LOGGER.info("the MD5 signature is {}different from the last read: reloading is {}required!", text, text);
		}

		LOGGER.info("is reload required: {}", reload);
		LOGGER.debug("reloadRequired - END");

		return reload;
	}

	private static String getMD5(File file) {
		LOGGER.trace("getMD5 - START");

		String md5 = "";

		try (InputStream is = new FileInputStream(file)) {
			md5 = DigestUtils.md5Hex(is);
		} catch (IOException e) {
			LOGGER.error("getMD5", e);
		}

		LOGGER.trace("getMD5 - END");

		return md5;
	}

	private void reloadTenantsIfRequired() {
		LOGGER.debug("reloadTenantsIfRequired - START");

		try {
			if (needsToBeReloaded()) {
				LOGGER.info("reloading tenant info...");
				loadTenants();
				LOGGER.info("reloading tenant info...DONE");
			} else {
				LOGGER.debug("tenant cache is up to date");
			}
		} catch (IOException e) {
			LOGGER.error("Unable to load tenant file", e);
		}

		LOGGER.debug("reloadTenantsIfRequired - END");
	}

	public List<String> getTenantsIds() {
		List<String> keys = null;

		Map<String, TenantInfo> tenants = tenantMap.getTenants();
		if (tenants != null) {
			keys = tenants.keySet().stream().collect(Collectors.toList());
		} else {
			keys = Collections.emptyList();
		}

		return keys;
	}

	public Optional<TenantInfo> getInfo(String tenantName) {
		LOGGER.debug("getInfo - START");

		LOGGER.debug("tenantName: {}", tenantName);

		reloadTenantsIfRequired();

		TenantInfo tenantInfo = null;

		if (StringUtils.isNotBlank(tenantName)) {
			Map<String, TenantInfo> tenants = tenantMap.getTenants();
			TenantInfo info = tenants == null ? null : tenants.get(tenantName);
			tenantInfo = info == null ? null : ModelMapperHelper.map(info, TenantInfo.class);
		}

		Optional<TenantInfo> value = Optional.ofNullable(tenantInfo);

		LOGGER.debug("TenantInfo: {} -> {}", tenantName, value);
		LOGGER.debug("getInfo - END");

		return value;
	}

	/**
	 * 
	 * @return The tenant info about current tenant
	 */
	public Optional<TenantInfo> getInfo() {
		LOGGER.debug("getInfo - START");

		Optional<TenantInfo> value = getInfo(getTenantName());

		LOGGER.debug("getInfo - END");

		return value;
	}

	public List<String> getKeycloakIds() {
		LOGGER.debug("getKeycloakIds - START");

		List<String> list = new ArrayList<>();

		Map<String, TenantInfo> tenants = tenantMap.getTenants();
		if (tenants != null) {
			list.addAll( //
					tenants.values().stream() //
							.map(TenantInfo::getKeycloak) //
							.collect(Collectors.toList()) //
			);
		}

		LOGGER.debug("getKeycloakIds - END");

		return list;
	}

	public Optional<String> getSchemaFromKeycloakId(String keycloakId) {
		LOGGER.debug("getSchemaFromKeycloakId - START");

		String schemaName = null;

		Optional<TenantInfo> tenantInfo = getInfoFromKeycloakId(keycloakId);
		if (tenantInfo.isPresent()) {
			schemaName = tenantInfo.map(elem -> elem.getDbSchema()).orElse(null);
		}

		Optional<String> value = Optional.ofNullable(schemaName);

		LOGGER.debug("value: {}", value);
		LOGGER.debug("getSchemaFromKeycloakId - END");

		return value;
	}

	public Optional<TenantInfo> getInfoFromKeycloakId(String keycloakId) {
		LOGGER.debug("getInfoFromKeycloakId - START");

		reloadTenantsIfRequired();

		TenantInfo tenantInfo = null;
		if (StringUtils.isNotBlank(keycloakId)) {
			Map<String, TenantInfo> tenants = tenantMap.getTenants();
			if (tenants != null) {
				tenantInfo = tenants.values().stream() //
						.filter(item -> keycloakId.equalsIgnoreCase(item.getKeycloak())) //
						.findFirst() //
						.orElse(null);
			}
		}

		Optional<TenantInfo> value = Optional.ofNullable(tenantInfo);

		LOGGER.debug("TenantInfo: {}", value);
		LOGGER.debug("getInfoFromKeycloakId - END");

		return value;
	}

	private String getTenantName() {
		LOGGER.debug("getTenantName - START");

		reloadTenantsIfRequired();

		String tenantName = Optional.ofNullable(tenantStorage.getTenantName()) //
				.map(String::toLowerCase) //
				.orElse("NA");

		LOGGER.debug("tenantName: {}", tenantName);
		LOGGER.debug("getTenantName - END");

		return tenantName;
	}

	public Optional<String> setTenantFromCodiceCatastale(String codiceCatastale) throws MissingTenantException {
		LOGGER.debug("setTenantFromCodiceCatastale - START");

		reloadTenantsIfRequired();

		Optional<String> value = setTenantFromParam(codiceCatastale, new ITenantFilter() {

			@Override
			public String getValue(TenantInfo tenantInfo) {
				return tenantInfo.getCodCatastale();
			}

			@Override
			public String getLabel() {
				return "codice catastale";
			}
		});

		LOGGER.debug("value: {}", value);
		LOGGER.debug("setTenantFromCodiceCatastale - END");

		return value;
	}

	public Optional<String> setTenantFromCodiceAmministrazione(String ipaAoo) throws MissingTenantException {
		LOGGER.debug("setTenantFromCodiceAmministrazione - START");

		reloadTenantsIfRequired();

		Optional<String> value = setTenantFromParam(ipaAoo, new ITenantFilter() {

			@Override
			public String getValue(TenantInfo tenantInfo) {
				return tenantInfo.getIpaAoo();
			}

			@Override
			public String getLabel() {
				return "ipa AOO";
			}
		});

		LOGGER.debug("value: {}", value);
		LOGGER.debug("setTenantFromCodiceAmministrazione - END");

		return value;
	}

	public Optional<String> setTenantFromIstat(String istat) throws MissingTenantException {
		LOGGER.debug("setTenantFromIstat - START");

		reloadTenantsIfRequired();

		Optional<String> value = setTenantFromParam(istat, new ITenantFilter() {

			@Override
			public String getValue(TenantInfo tenantInfo) {
				return tenantInfo.getIstat();
			}

			@Override
			public String getLabel() {
				return "ISTAT";
			}
		});

		LOGGER.debug("value: {}", value);
		LOGGER.debug("setTenantFromIstat - END");

		return value;
	}

	private Optional<String> setTenantFromParam(String value, ITenantFilter tenantFilter) throws MissingTenantException {
		LOGGER.debug("setTenantFromParam - START");

		reloadTenantsIfRequired();

		Optional<String> tenant = tenantMap.getTenants().entrySet() //
				.stream() //
				.filter(e -> value.equalsIgnoreCase(tenantFilter.getValue(e.getValue()))) //
				.map(e -> e.getKey()) //
				.findFirst();

		if (tenant.isPresent()) {
			tenantStorage.setTenantName(tenant.get().toLowerCase()); // loadTenants()
			LOGGER.debug("CMDB Set Tenant to: " + tenant.get().toLowerCase());
		} else {
			LOGGER.warn("Unable to find tenant for {}: {}", tenantFilter.getLabel(), value);
			throw new MissingTenantException("Unable to find tenant for " + tenantFilter.getLabel() + ": " + value);
		}

		LOGGER.debug("tenant: {}", tenant);
		LOGGER.debug("setTenantFromParam - END");

		return tenant;
	}

	/**
	 * 
	 * Retrieve generic information stored inside the additionalInfo field of the TenantMap.
	 * 
	 * 
	 * @param <T>
	 *            the type of the information we want to retrieve
	 * @param keycloakId
	 *            the id of the tenant in keycloak (Name of the group)
	 * @param clasz
	 *            The class of the object we want to retrieve
	 * @return An Optional object containing the require info if present
	 */
	public <T> Optional<T> getAdditionalInfoFromKey(String keycloakId, Class<T> clasz) {
		LOGGER.debug("getAdditionalInfoFromKey - START");

		T additionalInfo = null;

		if (StringUtils.isNotBlank(keycloakId)) {
			Optional<TenantInfo> tenantInfo = getInfoFromKeycloakId(keycloakId);
			if (tenantInfo.isPresent()) {
				Optional<Object> infoMap = Optional.ofNullable(tenantInfo.get() //
						.getAdditionalInfo() //
						.get(clasz.getName()) //
				);
				if (infoMap.isPresent()) {
					try {
						Object genericAdditionalInfo = infoMap.get();
						ObjectMapper mapper = new ObjectMapper();
						String additionalInfoAsText = mapper.writeValueAsString(genericAdditionalInfo);
						additionalInfo = mapper.readValue(additionalInfoAsText, clasz);
					} catch (JsonProcessingException e) {
						LOGGER.error("Unable to parse the additional info map to a class", e);
					}
				}
			}
		}

		Optional<T> value = Optional.ofNullable(additionalInfo);

		LOGGER.debug("getAdditionalInfoFromKey - additionalInfo={}", value);
		LOGGER.debug("getAdditionalInfoFromKey - END");

		return value;
	}

	public <T> Optional<T> getAdditionalInfo(Class<T> clasz) {
		LOGGER.debug("getAdditionalInfo - START");

		reloadTenantsIfRequired();

		Optional<T> value = getAdditionalInfoFromKey(getTenantName(), clasz);

		LOGGER.debug("getAdditionalInfo - additionalInfo: {}", value);
		LOGGER.debug("getAdditionalInfo - END");

		return value;
	}

	public void setTenant(String tenant) {
		LOGGER.debug("setTenant - START");

		reloadTenantsIfRequired();

		tenantStorage.setTenantName(tenant);

		LOGGER.debug("setTenant - END");
	}

	public void addListener(ITenantConfiguratorListener listener) {
		LOGGER.debug("addListener - START");

		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}

		LOGGER.info("addListener - listeners count (after adding): {}", listeners.size());

		LOGGER.debug("addListener - END");
	}

	public void removeListener(ITenantConfiguratorListener listener) {
		LOGGER.debug("removeListener - START");

		if (listener != null) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}

		LOGGER.info("removeListener - listeners count (after removing): {}", listeners.size());

		LOGGER.debug("removeListener - END");
	}

	private void noytifyConfigurationEventToListeners(ConfigurationEvent event) {
		LOGGER.debug("noytifyConfigurationEventToListeners - START");

		synchronized (listeners) {
			listeners.forEach(listener -> {
				LOGGER.debug("noytifyConfigurationEventToListeners - event={}, listener={}", event, listener);

				try {
					switch (event) {
						case RELOADING:
							listener.onReloading(tenantMap);
							break;

						case RELOADED:
							listener.onReloaded(tenantMap);
							break;

						case RELOADIN_ERROR:
							listener.onReloadingError();
							break;
					}
				} catch (Exception e) {
					LOGGER.error("noytifyConfigurationEventToListeners - event={}, listener={}", event, listener, e);
				}
			});
		}

		LOGGER.debug("noytifyConfigurationEventToListeners - END");
	}
}
