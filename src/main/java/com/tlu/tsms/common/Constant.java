package com.tlu.tsms.common;

public interface Constant {

    int MAX_DEVICE = 3;
    int SESSION_TIMEOUT = 7200;
    int MAX_LOGIN = 5;
    int LOCK_TIME = 300;
    int BCRYPT_STRENGTH = 12;
    int TIME_TO_LIVE = 30;
    int DEFAULT_ATTEMPT = 0;
    int MAX_STR_LEN = 800;
    int MAX_MAP_ENTRIES = 50;
    int MAX_DEPTH = 2;
    int MAX_COLLECTION_ITEMS = 30;

    String ROLE = "role";
    String USER_AGENT = "User-Agent";
    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
    String APPLICATION_JSON = "application/json";
    String ID = "id";
    String DESC = "desc";
    String ASC = "asc";
    String DEFAULT_PAGE = "0";
    String DEFAULT_PAGE_SIZE = "10";
    String ANONYMOUS_USER = "anonymousUser";
    String MDC_TRACE_ID = "traceId";
    String X_FORWARDED_FOR = "X-Forwarded-For";
    String COMMA = ",";
    String X_REAL_IP = "X-Real-IP";
    String HEADER_REQUEST_ID = "X-Request-Id";
    String METHOD = "method";
    String ARGS_COUNT = "argsCount";
    String RESULT_TYPE = "resultType";
    String ERROR_MESSAGE = "errorMessage";
    String FALSE = "false";
    String TRUE = "true";
    String RESULT = "result";
    String ERROR = "error";
    String ACTOR = "actor";
    String REQUEST = "request";
    String LEVEL = "level";
    String MODULE = "module";
    String ACTION = "action";
    String ACTOR_NAME = "actorName";
    String TIMESTAMP = "timestamp";
    String MESSAGE = "message";
    String RESOURCE_TYPE = "resourceType";
}