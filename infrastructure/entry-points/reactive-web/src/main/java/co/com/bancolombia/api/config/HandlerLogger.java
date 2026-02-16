package co.com.bancolombia.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HandlerLogger {

    private static final String TRACE_ID_KEY = "traceId";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Tracer tracer;

    public HandlerLogger(Tracer tracer) {
        this.tracer = tracer;
    }

    public void logRequest(String operation, Object body) {
        setTraceId();
        log.info("Request {}: {}", operation, toJson(body));
    }

    public void logResponse(String operation, Object body) {
        setTraceId();
        log.info("Response {}: {}", operation, toJson(body));
    }

    private void setTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            MDC.put(TRACE_ID_KEY, currentSpan.context().traceId());
        }
    }

    private String toJson(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
