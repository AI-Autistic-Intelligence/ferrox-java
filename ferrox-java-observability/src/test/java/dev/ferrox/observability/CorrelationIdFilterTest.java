package dev.ferrox.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    @Test
    void testCorrelationIdInjected() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                // Assert MDC inside the chain execution
                assertNotNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertNotNull(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        // Ensure MDC is cleared after request
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));
    }
}
