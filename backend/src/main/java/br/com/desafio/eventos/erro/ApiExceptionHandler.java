package br.com.desafio.eventos.erro;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail tratarStatusDaAplicacao(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String detalhe = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
        return criarProblema(status, detalhe, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarCamposInvalidos(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos informados são inválidos.",
                request
        );
        List<Map<String, String>> erros = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> Map.of(
                        "campo", erro.getField(),
                        "mensagem", erro.getDefaultMessage() == null
                                ? "Valor inválido."
                                : erro.getDefaultMessage()
                ))
                .toList();
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail tratarRestricoesInvalidas(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Um ou mais valores informados são inválidos.",
                request
        );
        List<Map<String, String>> erros = exception.getConstraintViolations()
                .stream()
                .map(violacao -> Map.of(
                        "campo", violacao.getPropertyPath().toString(),
                        "mensagem", violacao.getMessage()
                ))
                .toList();
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarCorpoInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return criarProblema(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição está malformado ou contém valores incompatíveis.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail tratarTipoDeConteudoNaoSuportado(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "O tipo de conteúdo da requisição não é suportado.",
                request
        );
        problema.setProperty("tipoEsperado", MediaType.APPLICATION_JSON_VALUE);
        return problema;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail tratarParametroObrigatorioAusente(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Um parâmetro obrigatório da requisição não foi informado.",
                request
        );
        problema.setProperty("parametro", exception.getParameterName());
        return problema;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tratarParametroInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.BAD_REQUEST,
                "Um parâmetro da requisição possui formato inválido.",
                request
        );
        problema.setProperty("parametro", exception.getName());
        return problema;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail tratarMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problema = criarProblema(
                HttpStatus.METHOD_NOT_ALLOWED,
                "O método HTTP não é permitido para este recurso.",
                request
        );
        if (exception.getSupportedHttpMethods() != null) {
            problema.setProperty(
                    "metodosPermitidos",
                    exception.getSupportedHttpMethods().stream().map(Object::toString).sorted().toList()
            );
        }
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return criarProblema(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception exception, HttpServletRequest request) {
        LOGGER.error("Erro inesperado ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);
        return criarProblema(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado ao processar a requisição.",
                request
        );
    }

    private ProblemDetail criarProblema(HttpStatus status, String detalhe, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setInstance(URI.create(request.getRequestURI()));
        return problema;
    }
}
