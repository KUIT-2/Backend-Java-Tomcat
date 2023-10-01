package http.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestUtils {
    public static Map<String, String> getQueryParameter(String queryString) {
        try {
            String[] queryStrings = queryString.split("&");     // 각 queryStrings는 쿼리 문자열을 '&'문자를 기준으로 나눈것

            return Arrays.stream(queryStrings)
                    .map(q -> q.split("="))
                    .collect(Collectors.toMap(queries -> queries[0], queries -> queries[1]));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
