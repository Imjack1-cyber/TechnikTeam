package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import de.technikteam.dao.GeoIpRuleDAO;
import de.technikteam.model.GeoIpRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GeoIpService {

    private static final Logger logger = LogManager.getLogger(GeoIpService.class);
    private final DatabaseReader databaseReader;
    private final GeoIpRuleDAO geoIpRuleDAO;
    private final LoadingCache<String, Map<String, String>> geoIpRulesCache;

    @Autowired
    public GeoIpService(GeoIpRuleDAO geoIpRuleDAO, @Value("${geoip.database.path}") String databasePath) {
        this.geoIpRuleDAO = geoIpRuleDAO;
        File database = new File(databasePath);
        if (!database.exists()) {
            logger.fatal("GeoIP database file not found at path: {}. GeoIP features will be disabled.", databasePath);
            this.databaseReader = null;
        } else {
            try {
                this.databaseReader = new DatabaseReader.Builder(database).withCache(new CHMCache()).build();
                logger.info("Successfully loaded GeoIP database from: {}", databasePath);
            } catch (IOException e) {
                logger.fatal("Failed to load GeoIP database from path: {}", databasePath, e);
                throw new RuntimeException("Could not initialize GeoIP database reader", e);
            }
        }
        this.geoIpRulesCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> fetchRulesFromDb());
    }

    private Map<String, String> fetchRulesFromDb() {
        return geoIpRuleDAO.findAllRules().stream()
                .collect(Collectors.toMap(GeoIpRule::getCountryCode, GeoIpRule::getRuleType));
    }

    public String getCountryCode(String ipAddress) {
        if (databaseReader == null || ipAddress == null) {
            return null;
        }
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CountryResponse response = databaseReader.country(ip);
            return response.getCountry().getIsoCode(); // e.g., "US", "DE"
        } catch (IOException | GeoIp2Exception e) {
            logger.warn("Could not resolve GeoIP information for IP {}: {}", ipAddress, e.getMessage());
            return null;
        }
    }

    public boolean isIpBlocked(String ipAddress) {
        String countryCode = getCountryCode(ipAddress);
        if (countryCode == null) {
            // Failsafe: if we can't determine the country, we don't block.
            return false;
        }
        Map<String, String> rules = geoIpRulesCache.get("rules");
        String rule = rules.get(countryCode);
        return "BLOCK".equals(rule);
    }
}