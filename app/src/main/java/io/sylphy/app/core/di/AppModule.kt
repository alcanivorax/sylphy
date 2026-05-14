package io.sylphy.app.core.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sylphy.app.data.local.db.SylphyDatabase
import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.dao.PlaylistDao
import io.sylphy.app.data.local.db.dao.QueueDao
import io.sylphy.app.data.local.db.dao.SessionDao
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.repository.TrackRepositoryImpl
import io.sylphy.app.domain.repository.TrackRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMediaControllerProvider(
        @ApplicationContext ctx: Context,
    ): MediaControllerProvider = MediaControllerProvider(ctx)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SylphyDatabase =
        Room.databaseBuilder(ctx, SylphyDatabase::class.java, "sylphy.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideTrackDao(db: SylphyDatabase): TrackDao       = db.trackDao()
    @Provides fun provideAlbumDao(db: SylphyDatabase): AlbumDao       = db.albumDao()
    @Provides fun provideArtistDao(db: SylphyDatabase): ArtistDao     = db.artistDao()
    @Provides fun providePlaylistDao(db: SylphyDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideSessionDao(db: SylphyDatabase): SessionDao   = db.sessionDao()
    @Provides fun provideQueueDao(db: SylphyDatabase): QueueDao       = db.queueDao()
}

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext ctx: Context): ExoPlayer =
        ExoPlayer.Builder(ctx)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

    // Bind ExoPlayer as Player so use cases can depend on the interface
    @Provides
    fun providePlayer(exoPlayer: ExoPlayer): Player = exoPlayer
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository
}
