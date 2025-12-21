package server.core;

public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        OPTIONS,
        PATCH;

    public static HttpMethod fromString(String method) {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }
        throw new IllegalArgumentException("Unknown HTTP method: " + method);
    }

    public static boolean isGet(String request) {
        int indexOf = request.indexOf(GET.toString());
        return indexOf != -1;
    }

    public static  boolean isPost(String request) {
        int indexOf = request.indexOf(POST.toString());
        return indexOf != -1;
    }

    public static boolean isPut(String request) {
        int indexOf = request.indexOf(PUT.toString());
        return indexOf != -1;
    }

    public static boolean isDelete(String request) {
        int indexOf = request.indexOf(DELETE.toString());
        return indexOf != -1;
    }

    public static boolean isHead(String request) {
        int indexOf = request.indexOf(HEAD.toString());
        return indexOf != -1;
    }

    public static boolean isOptions(String request) {
        int indexOf = request.indexOf(OPTIONS.toString());
        return indexOf != -1;
    }

    public static boolean isPatch(String request) {
        int indexOf = request.indexOf(PATCH.toString());
        return indexOf != -1;
    }
}
