package com.simplepathstudios.pbr.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.simplepathstudios.pbr.api.model.AddToPlaylistPayload;
import com.simplepathstudios.pbr.api.model.AdminLog;
import com.simplepathstudios.pbr.api.model.AdminLogs;
import com.simplepathstudios.pbr.api.model.AlbumList;
import com.simplepathstudios.pbr.api.model.AlbumView;
import com.simplepathstudios.pbr.api.model.ArtistList;
import com.simplepathstudios.pbr.api.model.ArtistView;
import com.simplepathstudios.pbr.api.model.CategoryList;
import com.simplepathstudios.pbr.api.model.MusicFile;
import com.simplepathstudios.pbr.api.model.MusicPlaylist;
import com.simplepathstudios.pbr.api.model.MusicQueue;
import com.simplepathstudios.pbr.api.model.MusicQueuePayload;
import com.simplepathstudios.pbr.api.model.PlaylistAddResult;
import com.simplepathstudios.pbr.api.model.PlaylistList;
import com.simplepathstudios.pbr.api.model.PlaylistPayload;
import com.simplepathstudios.pbr.api.model.SearchResults;
import com.simplepathstudios.pbr.api.model.ServerInfo;
import com.simplepathstudios.pbr.api.model.UserList;


public interface ApiService {
    @GET("api/song/list")
    Call<List<MusicFile>> getSongList();

    @GET("api/album/list")
    Call<AlbumList> getAlbumList();

    @GET("api/artist/list")
    Call<ArtistList> getArtistList(@Query("category") String category);

    @GET("api/artist/view")
    Call<ArtistView> getArtist(@Query("artist") String artist);

    @GET("api/album/view")
    Call<AlbumView> getAlbum(@Query("albumSlug") String albumSlug);

    @GET("api/user/list")
    Call<UserList> getUserList();

    @GET("api/queue/{username}")
    Call<MusicQueue> getQueue(@Path("username") String username);

    @POST("/api/queue/{username}")
    Call<MusicQueuePayload> setQueue(@Path("username") String username, @Body MusicQueuePayload queue);

    @DELETE("/api/queue/{username}")
    Call<MusicQueue> clearQueue(@Path("username") String username);

    @GET("/api/system/info")
    Call<ServerInfo> getServerInfo();

    @GET("/api/search")
    Call<SearchResults> search(@Query("query") String query);

    @GET("/api/playlist/list")
    Call<PlaylistList> getPlaylists();

    @GET("/api/playlist/view")
    Call<MusicPlaylist> getPlaylist(@Query("playlistId") String playlistId);

    @POST("/api/playlist/add")
    Call<PlaylistAddResult> addToPlaylist(@Body AddToPlaylistPayload payload);

    @POST("/api/admin/log")
    Call<AdminLogs> writeLog(@Body AdminLog log);

    @GET("/api/admin/log")
    Call<AdminLogs> readLogs(@Query("clientId") String clientId);

    @POST("/api/playlist")
    Call<MusicPlaylist> savePlaylist(@Body PlaylistPayload playlist);

    @GET("/api/random/list")
    Call<MusicPlaylist> getRandomList();

    @GET("/api/category/list")
    Call<CategoryList> getCategoryList();
}