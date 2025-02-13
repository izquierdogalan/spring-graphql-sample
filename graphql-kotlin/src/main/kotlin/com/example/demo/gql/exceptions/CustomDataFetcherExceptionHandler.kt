package com.example.demo.gql.exceptions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import graphql.ErrorType
import graphql.ErrorType.ValidationError
import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.execution.*
import graphql.language.SourceLocation
import org.slf4j.LoggerFactory

class CustomDataFetcherExceptionHandler : DataFetcherExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(CustomDataFetcherExceptionHandler::class.java)
    }

    private val simpleDataFetcherExceptionHandler = SimpleDataFetcherExceptionHandler()

    override fun onException(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
        val exception = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path

        return when (exception) {
            is ValidationException -> {
                val error: GraphQLError = ValidationDataFetchingGraphQLError(
                    exception.constraintErrors,
                    path,
                    exception,
                    sourceLocation
                )
                log.debug(error.message, exception)
                return DataFetcherExceptionHandlerResult.newResult().error(error).build()
            }

            else -> simpleDataFetcherExceptionHandler.onException(handlerParameters)
        }
    }
}

@JsonIgnoreProperties("exception")
class ValidationDataFetchingGraphQLError(
    val constraintErrors: List<ConstraintError>,
    path: ResultPath,
    exception: Throwable,
    sourceLocation: SourceLocation
) : ExceptionWhileDataFetching(
    path,
    exception,
    sourceLocation
) {
    override fun getErrorType(): ErrorType = ValidationError
}