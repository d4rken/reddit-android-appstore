package subreddit.android.appstore.backend.github;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import subreddit.android.appstore.BuildConfig;
import subreddit.android.appstore.backend.UserAgentInterceptor;

public class LiveSelfUpdater implements SelfUpdater {
    private static final String BASEURL = "https://api.github.com/";
    private final ReleaseApi releaseApi;
    private Observable<Release> latestReleaseCache;
    private Observable<List<Contributor>> latestContributorsCache;

    public LiveSelfUpdater(UserAgentInterceptor userAgent) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.addInterceptor(userAgent);
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASEURL)
                .build();
        releaseApi = retrofit.create(ReleaseApi.class);
    }

    interface ReleaseApi {
        @GET("repos/d4rken/reddit-android-appstore/releases/latest")
        Observable<Release> getLatestRelease();

        @GET("repos/d4rken/reddit-android-appstore/contributors")
        Observable<List<Contributor>> getContributors();
    }

    @Override
    public Observable<Release> getLatestRelease() {
        if (latestReleaseCache == null) latestReleaseCache = releaseApi.getLatestRelease().cache();
        return latestReleaseCache;
    }

    @Override
    public Observable<List<Contributor>> getContributors() {
        if (latestContributorsCache == null) latestContributorsCache = releaseApi.getContributors().cache();
        return latestContributorsCache;
    }
}
