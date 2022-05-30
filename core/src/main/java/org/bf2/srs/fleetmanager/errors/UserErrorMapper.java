package org.bf2.srs.fleetmanager.errors;

import com.fasterxml.jackson.core.JsonParseException;
import org.bf2.srs.fleetmanager.common.errors.UserErrorCode;
import org.bf2.srs.fleetmanager.common.errors.UserErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.validation.ValidationException;
import javax.ws.rs.NotSupportedException;


/**
 * This mapper maps exceptions to user errors for cases where the underlying exception
 * has not been defined by us, and cannot implement {@link org.bf2.srs.fleetmanager.common.errors.UserError}.
 *
 * @author Jakub Senko <jsenko@redhat.com>
 */
public class UserErrorMapper {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(UserErrorMapper.class);

    private static final Map<Class<? extends Exception>, Function<Exception, UserErrorInfo>> MAP;

    static {
        // NOTE: Subclasses of the entry will be matched as well.
        // Make sure that if a more specific exception requires a different user error info,
        // it is inserted first.
        Map<Class<? extends Exception>, Function<Exception, UserErrorInfo>> map = new LinkedHashMap<>();

        map.put(DateTimeParseException.class, ex -> UserErrorInfo.create(UserErrorCode.ERROR_FORMAT_DATETIME));
        map.put(ValidationException.class, ex -> UserErrorInfo.create(UserErrorCode.ERROR_REQUEST_CONTENT_INVALID));
        map.put(JsonParseException.class, ex -> UserErrorInfo.create(UserErrorCode.ERROR_FORMAT_JSON));
        map.put(NotSupportedException.class, ex -> UserErrorInfo.create(UserErrorCode.ERROR_REQUEST_UNSUPPORTED_MEDIA_TYPE));

        MAP = Collections.unmodifiableMap(map);
    }

    public static boolean hasMapping(Class<? extends Exception> clazz) {
        for (Class<? extends Exception> key : MAP.keySet()) {
            if (key.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static UserErrorInfo getMapping(Exception ex) {
        for (Map.Entry<Class<? extends Exception>, Function<Exception, UserErrorInfo>> entry : MAP.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                return entry.getValue().apply(ex);
            }
        }
        throw new IllegalArgumentException("No mapping for exception", ex);
    }

    private UserErrorMapper() {
    }
}
