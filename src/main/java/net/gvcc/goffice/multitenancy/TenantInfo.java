package net.gvcc.goffice.multitenancy;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantInfo {
	String keycloak;
	String belfiore;
	String istat;
	String dbSchema;
	String ldapO;
	String ipaAoo;
	String codCatastale;

	@Builder.Default
	Map<String, Object> additionalInfo = new HashMap<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("=== TenantInfo ===") //
				.append("\nkeycloak:.......").append(keycloak) //
				.append("\nbelfiore:.......").append(belfiore) //
				.append("\nistat:..........").append(istat) //
				.append("\ndbSchema:.......").append(dbSchema) //
				.append("\nldapO:..........").append(ldapO) //
				.append("\nipaAoo:.........").append(ipaAoo) //
				.append("\ncodCatastale:...").append(codCatastale);

		if (additionalInfo != null) {
			additionalInfo.forEach((key, value) -> {
				if (key.matches("(?i).*((password)|(pwd)|(passwd)).*")) { // if password key
					value = value == null ? "" : "******";
				}
				builder.append("\n[AI] ").append(key).append(": ").append(value);
			});
		}
		return builder.toString();
	}
}
