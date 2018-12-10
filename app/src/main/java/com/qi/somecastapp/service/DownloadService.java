package com.qi.somecastapp.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.qi.somecastapp.model.Episode;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.qi.somecastapp.utilities.SomePodcastAppConstants.KEY_EPISODE_META;

public class DownloadService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    HashMap<Long, MetaCorrectorRunnable> list = new HashMap<>();
    private DownloadManager downloadManager;
    private String TAG = DownloadService.class.getName();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Episode episode = msg.getData().getParcelable(KEY_EPISODE_META);
            Uri audioPath =Uri.parse(episode.getAudioPath());
            DownloadManager.Request request = new DownloadManager.Request(audioPath);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            request.setTitle(episode.getTitle());
            request.setDescription(episode.getDescription());
            request.setVisibleInDownloadsUi(true);
            String fileName = episode.getId() + ".mp3";
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, fileName);
            request.allowScanningByMediaScanner();
            long refId = downloadManager.enqueue(request);
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath() + "/" + fileName;
            MetaCorrectorRunnable corrector = new MetaCorrectorRunnable(filePath, episode, episode.getPodcastName());
            list.put(refId, corrector);
        }
    }

    public DownloadService() {
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "New download task added. Task list size: " + list.size());

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_EPISODE_META, intent.getParcelableExtra(KEY_EPISODE_META));
        msg.setData(bundle);
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "All download tasks done. Stopping service...");
        unregisterReceiver(onComplete);
    }

    class MetaCorrectorRunnable implements Runnable {
        private String mFilePath;
        private Episode mEpisode;
        private String mAlbum;

        public MetaCorrectorRunnable(String filePath, Episode episode, String podcastName) {
            this.mFilePath = filePath;
            this.mEpisode = episode;
            this.mAlbum = podcastName;
        }

        @Override
        public void run() {
            File src = new File(mFilePath);
            try {
                AudioFile file = AudioFileIO.read(src);
                Tag tag = file.getTag();
                tag.setField(FieldKey.TITLE,mEpisode.getTitle());
                tag.setField(FieldKey.ALBUM, mAlbum);
                file.commit();
            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TagException e) {
                e.printStackTrace();
            } catch (ReadOnlyFileException e) {
                e.printStackTrace();
            } catch (InvalidAudioFrameException e) {
                e.printStackTrace();
            } catch (CannotWriteException e) {
                e.printStackTrace();
            }

        }
    }


    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (referenceId != -1) {
                Log.d(TAG, "Setting metadata for task: " + referenceId);
                (new Thread(list.get(referenceId))).run();
                list.remove(referenceId);
            }
            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath() },
                    new String[]{ "audio/mp3", "*/*" },
                    new MediaScannerConnection.MediaScannerConnectionClient()
                    {
                        public void onMediaScannerConnected()
                        {
                        }
                        public void onScanCompleted(String path, Uri uri)
                        {
                        }
                    });
            Log.d(TAG, "Remaining metadata to be set: " + list.size());
            if (list.size() == 0) {
                stopSelf();
            }

        }
    };
}
