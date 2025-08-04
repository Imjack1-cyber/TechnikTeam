package de.technikteam.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class WebAuthnConfig {

	@Value("${webauthn.relying-party.id}")
	private String relyingPartyId;

	@Value("${webauthn.relying-party.name}")
	private String relyingPartyName;

	@Value("${webauthn.relying-party.origins}")
	private Set<String> relyingPartyOrigins;

	@Bean
	public RelyingParty relyingParty(CredentialRepository credentialRepository) {
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id(relyingPartyId).name(relyingPartyName)
				.build();

		return RelyingParty.builder().identity(rpIdentity).credentialRepository(credentialRepository)
				.origins(relyingPartyOrigins).build();
	}
}