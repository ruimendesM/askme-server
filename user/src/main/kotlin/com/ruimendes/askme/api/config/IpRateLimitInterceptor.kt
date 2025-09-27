package com.ruimendes.askme.api.config

import com.ruimendes.askme.domain.exception.RateLimitException
import com.ruimendes.askme.infra.rate_limiting.IpRateLimiter
import com.ruimendes.askme.infra.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${askme.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)
            if (annotation != null) {
                val clientIp = ipResolver.getClientIp(request)

                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        path = handler.getRelativePath(),
                        resetsIn = Duration.of(annotation.duration, annotation.unit.toChronoUnit()),
                        maxRequestsPerIp = annotation.requests,
                        action = { true }
                    )
                } catch (e: RateLimitException) {
                    response.sendError(429)
                    false
                }
            }
        }
        return true
    }
}

private fun HandlerMethod.getRelativePath(): String {
    val classMapping = beanType.getAnnotation(RequestMapping::class.java)
    val classPath = classMapping?.value?.firstOrNull() ?: ""

    val methodPath = method.getAnnotation(PostMapping::class.java)?.value?.firstOrNull()
        ?: method.getAnnotation(GetMapping::class.java)?.value?.firstOrNull()
        ?: method.getAnnotation(PutMapping::class.java)?.value?.firstOrNull()
        ?: method.getAnnotation(DeleteMapping::class.java)?.value?.firstOrNull()
        ?: method.getAnnotation(PatchMapping::class.java)?.value?.firstOrNull()
        ?: method.getAnnotation(RequestMapping::class.java)?.value?.firstOrNull()
        ?: ""

    return (classPath + methodPath).replace("//", "/")
}