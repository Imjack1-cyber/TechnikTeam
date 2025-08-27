package de.technikteam.model;

public class GeoIpRule {
    private String countryCode;
    private String ruleType; // "ALLOW" or "BLOCK"

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }
}