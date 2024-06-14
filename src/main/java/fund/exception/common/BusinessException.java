package fund.exception.common;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public class BusinessException extends RuntimeException {
	
	private static final long serialVersionUID = 2949492826807250259L;
	private final ExceptionType type;
	private final ImmutableMap<String, Object> params;
	
	public BusinessException(ExceptionType type, ImmutableMap<String, Object> params) {
		super(build(type, "", params, null));
		this.type = type;
		this.params = params;
	}
	
	public BusinessException(ExceptionType type, ImmutableMap<String, Object> params, Throwable cause) {
		super(build(type, "", params, cause), cause);
		this.type = type;
		this.params = params;
	}
	
	public BusinessException(String message, ImmutableMap<String, Object> params) {
		super(build(message, params));
		this.params = params;
		this.type = null;
	}
	
	public ExceptionType getType() {
		return this.type;
	}

	public ImmutableMap<String, Object> getParams() {
		return params;
	}
	
	private static String build(ExceptionType type, String message, Map<String, Object> params, Throwable cause) {
		StringBuilder builder = new StringBuilder(256);
		builder.append("type=[").append(type).append(']');
		if (!Strings.isNullOrEmpty(message)) {
			builder.append(" message=[").append(message).append(']');
		}
		if (params.size() > 0) {
			params.forEach(paramsConsumer(builder));	
		}
		if (cause != null) {
			builder.append(" cause=[").append(cause.getMessage()).append(']');
		}

		return builder.toString();
	}
	
	private static String build(String message, Map<String, Object> params) {
		StringBuilder builder = new StringBuilder(256);
		builder.append("message=[").append(message).append(']');
		params.forEach(paramsConsumer(builder));
		return builder.toString();
	}
	
	private static BiConsumer<String,Object> paramsConsumer(StringBuilder builder) {
		return (key, value) -> {
			builder.append(" [").append(key).append("]=[");
			if (value instanceof Optional) {
				Optional<?> op = (Optional<?>)value;
				if (op.isPresent()) {
					builder.append(op.get());	
				} else {
					builder.append("<<null>>");
				}
			} else {
				builder.append(value);
			}
			builder.append(']');
		};
	}

}
