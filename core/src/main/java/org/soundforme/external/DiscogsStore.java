package org.soundforme.external;

import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soundforme.external.model.ReleaseExternal;
import org.soundforme.external.model.ReleasesPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author NGorelov
 */
@Component
public class DiscogsStore {
    private static final Logger logger = LoggerFactory.getLogger(DiscogsStore.class);

    @Value("${api.url.context.pattern}")
    private String basicUrl;
    @Inject
    private Gson gson;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(20);

    private <T> Future<T> getResource(int id, Integer page, Class<T> resultType, String pattern) {
        checkArgument(id > 0, "Release id should be bigger then 1");
        checkArgument(page == null || page > 0, "Page number should be more then zero or null");

        String url = MessageFormat.format(pattern, "" + id);
        logger.debug("Collecting info from url: {}", url);

        return scheduledExecutorService.schedule(new RetriableTask<>(() -> {
            String result = getContent(url, page);
            return gson.fromJson(result, resultType);
        }), 1, TimeUnit.SECONDS);
    }

    public Future<ReleaseExternal> getReleaseResource(int id) {
        return getResource(id, null, ReleaseExternal.class, basicUrl + "releases/{0}");
    }

    public Future<ReleasesPage> getArtistReleasesPage(int id, int page) {
        return getResource(id, page, ReleasesPage.class, basicUrl + "artists/{0}/releases");
    }

    public Future<ReleasesPage> getLabelReleasesPage(int id, int page) {
        return getResource(id, page, ReleasesPage.class, basicUrl + "labels/{0}/releases");
    }

    public String getArtistNameById(int id) {
        String result = null;
        try {
            result = (String) getResource(id, null, Map.class, basicUrl + "artists/{0}").get().get("name");
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Artist with id {} not found", id, e);
        }
        return result;
    }

    public String getLabelTitleById(int id) {
        String result = null;
        try {
            result = (String) getResource(id, null, Map.class, basicUrl + "labels/{0}").get().get("name");
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Label with id {} not found", id, e);
        }
        return result;
    }

    private String getContent(String url, Integer page) {
        try {
            Connection connection = Jsoup.connect(url)
                    .header("User-Agent", "sound-for-me-app")
                    .ignoreContentType(true)
                    .timeout(50000);

            if(page != null){
                connection = connection
                        .data("page", page.toString())
                        .data("per_page", "100");
            }

            return connection.execute().body();
        } catch (IOException e) {
            logger.debug("Error on connecting to url {} with page {}", url, page);
            throw new DiscogsConnectionException("Error on connection to discogs", e);
        }
    }
}