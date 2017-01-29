package search.deezer.oliverdixon.dthoseartistsz.common;

import android.media.MediaPlayer;
import android.support.annotation.Nullable;

import search.deezer.oliverdixon.dthoseartistsz.models.TrackModel;

public class MusicPlayerSingleton {

    private ReferenceObserver<TrackModel> trackPlayingReferenceObserver = new ReferenceObserver<>();

    private @Nullable TrackModel currentPlayingTrack = null;
    private @Nullable MediaPlayer mediaPlayer;

    private static MusicPlayerSingleton ourInstance = new MusicPlayerSingleton();
    public static MusicPlayerSingleton getInstance() {
        return ourInstance;
    }

    private MusicPlayerSingleton() {
    }

    public void playTrack(final TrackModel trackModel, Action<Boolean> isPlaying) {
        if (isThisTrack(trackModel)) {

            if (isThisTrackPlaying(trackModel)) {
                pause();
                isPlaying.invoke(false);
            } else {
                resume();
                isPlaying.invoke(true);
            }

            trackPlayingReferenceObserver.emit(trackModel);

        } else {
            setupMediaPlayer(trackModel, isPlaying);
        }
    }

    private void setupMediaPlayer(final TrackModel trackModel, Action<Boolean> isPlaying) {

        stop();
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(trackModel.getPreview());
            mediaPlayer.prepare();

        } catch (Exception e) {
            Logger.logError("Unable to play track, preview link: " + trackModel.getPreview());
            e.printStackTrace();
            isPlaying.invoke(false);
            return;
        }

        mediaPlayer.start();
        isPlaying.invoke(true);
        this.currentPlayingTrack = trackModel;
    }

    public void listenForTrackChange(Action<TrackModel> trackModelAction) {
        trackPlayingReferenceObserver.subscribe(trackModelAction);
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        currentPlayingTrack = null;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            killPlayer();
        }
    }

    public void clearTracksListening() {
        trackPlayingReferenceObserver.clear();
    }

    private void killPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean isThisTrack(final TrackModel trackModel) {
        return currentPlayingTrack != null && currentPlayingTrack.getId().equals(trackModel.getId());
    }

    public boolean isThisTrackPlaying(final TrackModel trackModel) {
        return isThisTrack(trackModel) && mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
