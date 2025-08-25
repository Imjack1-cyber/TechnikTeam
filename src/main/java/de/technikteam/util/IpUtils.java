package de.technikteam.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IpUtils {
    private static final Logger logger = LogManager.getLogger(IpUtils.class);

    private IpUtils() {}

    public static String getSubnet(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }

        try {
            InetAddress addr = InetAddress.getByName(ipAddress);
            byte[] bytes = addr.getAddress();

            if (bytes.length == 4) { // IPv4
                // Return a /24 subnet
                String subnet = (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." + (bytes[2] & 0xFF) + ".0";
                logger.debug("Resolved IPv4 '{}' to /24 subnet '{}'", ipAddress, subnet);
                return subnet;
            } else if (bytes.length == 16) { // IPv6
                // Return a /64 subnet prefix
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8; i += 2) { // First 8 bytes for /64
                    sb.append(String.format("%02x%02x", bytes[i], bytes[i + 1]));
                    if (i < 6) {
                        sb.append(":");
                    }
                }
                String subnet = sb.toString();
                logger.debug("Resolved IPv6 '{}' to /64 subnet '{}'", ipAddress, subnet);
                return subnet;
            }
        } catch (UnknownHostException e) {
            logger.warn("Could not parse IP address '{}'. It will be treated as unknown.", ipAddress, e);
            return null;
        }
        return null;
    }

    public static boolean isSameSubnet(String ip1, String ip2) {
        String subnet1 = getSubnet(ip1);
        String subnet2 = getSubnet(ip2);
        return subnet1 != null && subnet1.equals(subnet2);
    }
}