package de.technikteam.service;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserAgentService {

    private final UserAgentAnalyzer userAgentAnalyzer;

    public UserAgentService() {
        this.userAgentAnalyzer = UserAgentAnalyzer
                .newBuilder()
                .withField(UserAgent.DEVICE_CLASS)
                .withField(UserAgent.AGENT_NAME_VERSION)
                .withField(UserAgent.OPERATING_SYSTEM_NAME_VERSION)
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
    }

    public Map<String, String> parseUserAgent(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return Map.of("deviceType", "Unknown", "browser", "Unknown", "os", "Unknown");
        }
        
        UserAgent agent = userAgentAnalyzer.parse(userAgentString);
        Map<String, String> result = new HashMap<>();
        
        result.put("deviceType", agent.getValue(UserAgent.DEVICE_CLASS));
        result.put("browser", agent.getValue(UserAgent.AGENT_NAME_VERSION));
        result.put("os", agent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION));

        return result;
    }
}