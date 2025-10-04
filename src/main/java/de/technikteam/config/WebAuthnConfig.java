package de.technikteam.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import de.technikteam.dao.PasskeyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebAuthnConfig {

    @Value("${webauthn.relying-party.id}")
    private String rpId;

    @Value("${webauthn.relying-party.name}")
    private String rpName;

    @Value("${webauthn.relying-party.origins}")
    private String rpOrigins;

    private final PasskeyDAO passkeyDAO;

    @Autowired
    public WebAuthnConfig(PasskeyDAO passkeyDAO) {
        this.passkeyDAO = passkeyDAO;
    }

    @Bean
    public RelyingParty relyingParty() {
        RelyingPartyIdentity identity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name(rpName)
                .build();

        Set<String> origins = new HashSet<>(Arrays.asList(rpOrigins.split(",")));

        return RelyingParty.builder()
                .identity(identity)
                .credentialRepository(passkeyDAO)
                .origins(origins)
                .build();
    }
}