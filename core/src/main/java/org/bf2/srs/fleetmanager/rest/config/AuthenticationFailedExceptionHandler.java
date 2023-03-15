package org.bf2.srs.fleetmanager.rest.config;

import io.quarkus.security.AuthenticationFailedException;
import io.vertx.ext.web.Router;
import org.bf2.srs.fleetmanager.rest.privateapi.beans.ErrorInfo1Rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class AuthenticationFailedExceptionHandler {

    public void init(@Observes Router router) {
        router.route().failureHandler(event -> {
            if (event.failed() && event.failure() instanceof AuthenticationFailedException) {
                ErrorInfo1Rest errorInfo = new ErrorInfo1Rest();
                errorInfo.setErrorCode(event.statusCode());
                errorInfo.setMessage(event.failure().getMessage());
                event.response().end(errorInfo.toString());
            } else {
                event.next();
            }
        });
    }
}
