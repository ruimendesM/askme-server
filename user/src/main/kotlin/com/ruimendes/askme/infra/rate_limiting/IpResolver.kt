package com.ruimendes.askme.infra.rate_limiting

import com.ruimendes.askme.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {
    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy // Already has CIDR: "192.168.1.0/24"
                proxy.count { it == ':' } >= 2 -> "$proxy/128" // IPv6: "2001:db8::1" → "2001:db8::1/128"
                else -> "$proxy/32" // IPv4: "192.168.1.1" → "192.168.1.1/32"
            }
            IpAddressMatcher(cidr)
        }

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        logger.warn("Remote address: $remoteAddr")

        if (!isFromTrustedProxy(remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }
            return remoteAddr
        }

        logger.warn("Is from trusted proxy: $remoteAddr. Going to extract client IP from headers")

        val clientIp = extractFromXRealIp(request, remoteAddr)

        if (clientIp != null) {
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }

        return clientIp ?: remoteAddr
    }

    private fun extractFromXRealIp(
        request: HttpServletRequest,
        proxyIp: String
    ): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            logger.warn("Header found X-Real-IP: $header from proxy $proxyIp")
            validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        } ?: run {
            logger.warn("X-Real-IP header not found from proxy $proxyIp")
            null
        }
    }

    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()

        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.warn("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddress = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> {
                    Inet4Address.getByName(trimmedIp)
                }

                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }

            }
            if (isPrivateIp(inetAddress.hostAddress)) {
                logger.warn("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddress.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { it.matches(ip) }
    }
}