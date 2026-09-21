package com.empresa_rentar.web_services.exception;

import com.empresa_rentar.web_services.exception.custom.BadRequestException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GraphQLExceptionHandler {
    @GraphQlExceptionHandler({BadRequestException.class, DateTimeParseException.class})
    public GraphQLError handleBadRequest(GraphqlErrorBuilder<?> errorBuilder, Exception ex) {
        return errorBuilder.message(ex.getMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }
}
