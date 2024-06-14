package fund.problem;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableMap;

final class ProblemImpl implements Problem {
	
	private final OffsetDateTime timestamp;
	private final URI type;
	private final String instance;
	private final HttpStatus status;
	private final String title;
	private final String detail;
    private final ImmutableMap<String, Object> params;
	private final String code;
	
	public ProblemImpl(OffsetDateTime timestamp, URI type, String instance, HttpStatus status, String title, String detail, String code, ImmutableMap<String, Object> params) {
		this.timestamp = timestamp;
		this.type = type;
		this.instance = instance;
		this.status = status;
		this.title = title;
		this.detail = detail;
		this.params = params;
		this.code = code;
	}
	
	@Override
	public URI getType() {
		return type;
	}
	
	@Override
	public String getInstance() {
		return this.instance;
	}

	@Override
	public String getDetail() {
		return this.detail;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public ImmutableMap<String, Object> getParams() {
		return params;
	}

	@Override
	public OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public ResponseEntity<Problem> toResponseEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return  new ResponseEntity<>(this, headers, status);
	}

	@Override
	public String toString() {
		return "ProblemImpl [timestamp=" + timestamp + ", type=" + type + ", instance=" + instance + ", status="
				+ status + ", title=" + title + ", detail=" + detail + ", params=" + params + ", code=" + code + "]";
	}

}
