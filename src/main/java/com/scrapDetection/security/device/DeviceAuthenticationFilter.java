package com.scrapDetection.security.device;

import com.scrapDetection.entity.Device;
import com.scrapDetection.entity.DeviceStatus;
import com.scrapDetection.repository.DeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/*
  Authenticates Raspberry Pi devices on requests carrying:
    X-Device-Id  : the device's numeric ID
    X-Device-Key : the device's raw API key

  On success, sets an Authentication with principal = Device and authority
  ROLE_DEVICE, and stamps lastSeenAt. On any failure (missing headers,
  unknown device, wrong key, non-ACTIVE status) this filter simply leaves
  the request unauthenticated and lets it fall through — SecurityConfig's
  .requestMatchers("/api/detections/**").hasRole("DEVICE") is what actually
  rejects it, same pattern as JwtAuthenticationFilter.
 */
@Component
@RequiredArgsConstructor
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    private static final String DEVICE_ID_HEADER  = "X-Device-Id";
    private static final String DEVICE_KEY_HEADER = "X-Device-Key";

    private final DeviceRepository deviceRepository;
    private final DeviceApiKeyService deviceApiKeyService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String deviceIdHeader = request.getHeader(DEVICE_ID_HEADER);
        String deviceKey      = request.getHeader(DEVICE_KEY_HEADER);

        if (StringUtils.hasText(deviceIdHeader) && StringUtils.hasText(deviceKey)) {
            try {
                Long deviceId = Long.parseLong(deviceIdHeader.trim());

                deviceRepository.findById(deviceId).ifPresent(device -> {
                    boolean statusOk = device.getStatus() == DeviceStatus.ACTIVE;
                    boolean keyOk    = deviceApiKeyService.matches(deviceKey, device.getApiKeyHash());

                    if (statusOk && keyOk) {
                        authenticate(device);
                        touchLastSeen(device);
                    } else {
                        System.out.println("Device Filter - authentication FAILED for deviceId=" + deviceId);
                    }
                });

            } catch (NumberFormatException ex) {
                System.out.println("Device Filter - malformed X-Device-Id header: " + deviceIdHeader);
            } catch (Exception ex) {
                System.out.println("Device Filter - ERROR: " + ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(Device device) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        device,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void touchLastSeen(Device device) {
        device.setLastSeenAt(LocalDateTime.now());
        deviceRepository.save(device);
    }
}
