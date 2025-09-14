package de.technikteam.service;

import org.springframework.stereotype.Service;

@Service
public class PrivacyPolicyService {

    private static final String CURRENT_POLICY_VERSION = "2025-09-10";

    public String getCurrentVersion() {
        return CURRENT_POLICY_VERSION;
    }
}