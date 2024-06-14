package fund.problem;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ProblemBuilder {
	
	private static final Set<String> RESERVED_PROPERTIES = ImmutableSet.of("type", "title", "status", "detail", "instance");

    private OffsetDateTime timestamp;
    protected URI type;
    protected String instance;
    private HttpStatus status;
    private String title;
    private String detail;
    private String code;
    private final Map<String, String> params = new LinkedHashMap<>();
    
    public ProblemBuilder with(HttpServletRequest req) {
        this.type = URI.create(req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath());

        String originalUri = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (originalUri != null) {
            this.instance = originalUri;
        } else {
            this.instance = req.getServletPath() +
                    (Strings.isNullOrEmpty(req.getPathInfo()) ? "" : req.getPathInfo()) +
                    (Strings.isNullOrEmpty(req.getQueryString()) ? "" : req.getQueryString());
        }
        return this;
    }

    public ProblemBuilder withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ProblemBuilder withType(URI type) {
        this.type = type;
        return  this;
    }

    public ProblemBuilder withInstance(String instance) {
        this.instance = instance;
        return  this;
    }

    public ProblemBuilder withStatus(HttpStatus status) {
        this.status = status;
        return  this;
    }

    public ProblemBuilder withTitle(String title) {
        this.title = title;
        return  this;
    }

    public ProblemBuilder withCode(String code) {
        this.code = code;
        return  this;
    }

    public ProblemBuilder withDetail(String detail) {
        this.detail = detail;
        return  this;
    }

    public ProblemBuilder withParameters(ImmutableMap<String, Object> map) {
        map.forEach((key, value) -> {
            with(key, value);
        });
        return  this;
    }

    public ProblemBuilder with(ImmutableMap<String, Object> map) {
        map.forEach(this::with);
        return  this;
    }

    public ProblemBuilder with(String key, Object value) {

        if (RESERVED_PROPERTIES.contains(key)) {
            return  this;
        }

        if (value == null) {
            this.params.put(key, "null");
            return this;
        }

        if (value instanceof Optional) {

            Optional<?> op = (Optional<?>) value;
            if (op.isPresent()) {
                this.params.put(key, op.get().toString());
            } else {
                this.params.put(key, "null");
            }
            return this;
        }

        this.params.put(key, value.toString());
        return this;
    }

    public Problem build() {
        if (this.timestamp == null) {
            this.timestamp = OffsetDateTime.now();
        }
        return new ProblemImpl(timestamp, type, instance, status, title, detail, code, ImmutableMap.<String, Object>builder().putAll(params).build());
    }

}
