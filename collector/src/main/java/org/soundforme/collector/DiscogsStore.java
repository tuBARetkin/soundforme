package org.soundforme.collector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.collector.model.PaginatedResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author NGorelov
 */
@Component
public class DiscogsStore {
    private static final Logger logger = LoggerFactory.getLogger(DiscogsStore.class);

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Resource
    private Map<Type, String> apiPatterns;
    @Inject
    private Gson gson;

    public <T> T getResource(int id, Integer page, Class<T> resultType) {
        checkNotNull(resultType, "result type should be defined");
        checkArgument(page == null || page > 0, "page number should be more then zero or null");
        checkArgument(apiPatterns.containsKey(resultType), "result type should be presented in api patterns");

        String url = MessageFormat.format(apiPatterns.get(resultType), id);
        String result = getContent(url, page);
        return gson.fromJson(result, resultType);
    }

    public <T> T getResource(int id, Class<T> resultType) {
        return getResource(id, null, resultType);
    }

    private String getContent(String url, Integer page) {
        try {
            Connection connection = Jsoup.connect(url)
                    .header("User-Agent", "sound-for-me-app")
                    .ignoreContentType(true);
            if(page != null){
                connection = connection
                        .data("page", page.toString())
                        .data("per_page", "100");
            }
            return connection.execute()
                    .body();
        } catch (IOException e) {
            throw new DiscogsConnectionException("Error on connection to discogs", e);
        }
    }
}