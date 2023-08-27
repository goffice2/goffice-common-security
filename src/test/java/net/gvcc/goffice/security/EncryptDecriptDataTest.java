///////////////////////////////////////////////////////////////////////////////////
// this test is out of date because the encription has been replaced with jasypt //
///////////////////////////////////////////////////////////////////////////////////
// package net.gvcc.goffice.security;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
//
// import java.security.InvalidAlgorithmParameterException;
// import java.security.InvalidKeyException;
// import java.security.NoSuchAlgorithmException;
// import java.security.spec.InvalidKeySpecException;
// import java.util.Base64;
// import java.util.Map;
// import java.util.Optional;
//
// import javax.crypto.BadPaddingException;
// import javax.crypto.IllegalBlockSizeException;
//
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.context.annotation.PropertySource;
// import org.springframework.context.annotation.PropertySources;
// import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
// import org.springframework.security.crypto.keygen.KeyGenerators;
//
// import net.gvcc.goffice.config.GofficeSecurityService;
// import net.gvcc.goffice.multitenancy.TenantInfo;
// import net.gvcc.goffice.multitenancy.TenantService;
//
// @PropertySources({ @PropertySource("classpath:application.properties"), @PropertySource("classpath:security.properties") })
// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// public class EncryptDecriptDataTest {
//
// @Autowired
// GofficeSecurityService service;
//
// @Autowired
// TenantService tenantService;
//
// @Value("${enc.password}")
// private String encpw;
//
// @Value("${enc.salt}")
// private String encSalt;
//
// @Value("${enc.iv}")
// private String encIv;
//
// // private static final Random RANDOM = new SecureRandom();
//
// @Test
// public void decryptReturnsOriginalString()
// throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
//
// // // String saltString = getNextSalt();
//
// // Encode String
// AesBytesEncryptor encryptor = new AesBytesEncryptor(encpw, encSalt);
// byte[] encryptedString = encryptor.encrypt(encpw.getBytes());
// String encodedEncryptedString = new String(Base64.getEncoder().encodeToString(encryptedString));
//
// // Get String from json
// Optional<TenantInfo> info = tenantService.getInfoFromKeycloakId("Bolzano");
// Map<String, Object> additionalInfo = info.get().getAdditionalInfo();
// assertNotNull(additionalInfo);
//
// @SuppressWarnings("unchecked")
// Map<String, String> d3ProxyTenantInfo = (Map<String, String>) additionalInfo.get("net.gvcc.goffice.service.documentmanagement.d3Proxy.D3ProxyTenantInfo");
// assertNotNull(d3ProxyTenantInfo);
//
// encodedEncryptedString = d3ProxyTenantInfo.get("dmsPassword");
//
// // Decode String
// byte[] encodedEncryptedbytes = Base64.getDecoder().decode(encodedEncryptedString);
// byte[] result = encryptor.decrypt(encodedEncryptedbytes);
// String resultString = new String(result);
// //
// assertEquals(encpw, resultString);
// }
//
// /**
// * Returns a random salt to be used to hash a password.
// *
// * @return a 16 bytes random salt
// */
// public static String getNextSalt() {
// return KeyGenerators.string().generateKey();
// }
//
// }
