package de.frauas.notenmodulservice.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * Globaler Exception Handler für GraphQL
 * Behandelt alle Exceptions und wandelt sie in GraphQL-Fehler um
 */
@Component
@Slf4j
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("GraphQL Fehler aufgetreten: {}", ex.getMessage());
        String rawMessage = ex.getMessage() != null ? ex.getMessage() : "";

        // NotFoundException -> NOT_FOUND
        if (ex instanceof NotFoundException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        // ValidationException -> BAD_REQUEST  
        if (ex instanceof ValidationException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        // ConflictException -> BAD_REQUEST (GraphQL hat keinen CONFLICT-Typ)
        if (ex instanceof ConflictException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(java.util.Map.of("code", "CONFLICT"))
                .build();
        }

        // AccessDeniedException -> FORBIDDEN
        if (ex instanceof AccessDeniedException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.FORBIDDEN)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        // InvalidStateException -> BAD_REQUEST mit speziellem Code
        if (ex instanceof InvalidStateException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(java.util.Map.of("code", "INVALID_STATE"))
                .build();
        }

        // AuthenticationException -> UNAUTHORIZED
        if (ex instanceof AuthenticationException) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        // Spezialfall: fehlender Context-Wert (z.B. jwtUser)
        if (ex instanceof IllegalStateException && rawMessage.startsWith("Missing required context value")) {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.UNAUTHORIZED)
                .message("Authentifizierung fehlt oder ist ungültig. Bitte mit gültigem Token aufrufen.")
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        if (ex instanceof IllegalArgumentException) {
            // Validierungs-/Requestfehler: konkrete Meldung durchreichen
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(rawMessage)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        if (ex instanceof RuntimeException) {
            // Eigene Business-Fehler anhand der Meldung einordnen (Legacy-Kompatibilität)
            if (rawMessage.startsWith("Nicht authentifiziert")) {
                return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message("Nicht authentifiziert: Bitte melden Sie sich an und senden ein gültiges Token.")
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            }
            if (rawMessage.startsWith("Zugriff verweigert")) {
                return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.FORBIDDEN)
                    .message(rawMessage)
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            }

            // Alle anderen Runtime-Fehler: generische, aber freundliche Meldung
            log.warn("Unbehandelte RuntimeException: ", ex);
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Ein technischer Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.")
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
        }

        // Fallback für alle anderen Fehlerarten
        log.warn("Unbekannter Fehlertyp: ", ex);
        return GraphqlErrorBuilder.newError()
            .errorType(ErrorType.INTERNAL_ERROR)
            .message("Ein unerwarteter Fehler ist aufgetreten")
            .path(env.getExecutionStepInfo().getPath())
            .location(env.getField().getSourceLocation())
            .build();
    }
}