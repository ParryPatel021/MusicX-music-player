package com.rks.musicx.ui.homeWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.widget.RemoteViews;

import com.rks.musicx.R;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.services.MediaPlayerSingleton;
import com.rks.musicx.services.MusicXService;

import static com.rks.musicx.misc.utils.Constants.ACTION_COMMAND;
import static com.rks.musicx.misc.utils.Constants.ACTION_FAV;
import static com.rks.musicx.misc.utils.Constants.ACTION_NEXT;
import static com.rks.musicx.misc.utils.Constants.ACTION_PLAYINGVIEW;
import static com.rks.musicx.misc.utils.Constants.ACTION_PREVIOUS;
import static com.rks.musicx.misc.utils.Constants.ACTION_TOGGLE;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;

/*
 * Created by Coolalien on 02/05/2017.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MusicxWidget4x2 extends AppWidgetProvider {

    private static MusicxWidget4x2  sInstance;
    private Handler handler = new Handler();

    public static synchronized MusicxWidget4x2 getInstance() {
        if (sInstance == null) {
            sInstance = new MusicxWidget4x2 ();
        }
        return sInstance;
    }

    public void musicxWidgetUpdate(MusicXService musicXService, int updatdeID[]) {
        if (musicXService == null) {
            return;
        }
        RemoteViews remoteViews = new RemoteViews(musicXService.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.title, musicXService.getsongTitle());
        remoteViews.setTextViewText(R.id.artist, musicXService.getsongArtistName());
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoaderBitmapPalette(musicXService, musicXService.getsongAlbumName(), musicXService.getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        int colors[] = Helper.getAvailableColor(musicXService, palette);
                        remoteViews.setInt(R.id.item_view, "setBackgroundColor", colors[0]);
                        remoteViews.setInt(R.id.title, "setTextColor", Color.WHITE);
                        remoteViews.setInt(R.id.artist, "setTextColor", Color.WHITE);
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                        pushUpdate(musicXService, updatdeID, remoteViews);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                        pushUpdate(musicXService, updatdeID, remoteViews);
                    }
                });

            }
        });
        FavHelper favHelper = new FavHelper(musicXService);
        if (favHelper.isFavorite(Extras.getInstance().getSongId(musicXService.getsongId()))) {
            remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite);
        } else {
            remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite_outline);
        }
        controls(remoteViews, musicXService);
        pushUpdate(musicXService, updatdeID, remoteViews);
    }

    private void defaultAppWidget(Context context, int[] appWidgetIds) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.title, "Hello");
        views.setTextViewText(R.id.artist, "Adele");
        controls(views,context);
        pushUpdate(context, appWidgetIds, views);
    }

    private void pushUpdate(Context context, int[] appWidgetIds, RemoteViews views) {
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            gm.updateAppWidget(new ComponentName(context, this.getClass()), views);
        }
    }
    private void controls(RemoteViews remoteViews, Context context) {
        PendingIntent clickedview = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_PLAYINGVIEW), 0);
        PendingIntent nextIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_NEXT), 0);
        PendingIntent previousIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_PREVIOUS), 0);
        PendingIntent toggleIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_TOGGLE), 0);
        PendingIntent savefavIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_FAV), 0);

        remoteViews.setOnClickPendingIntent(R.id.artwork, clickedview);
        remoteViews.setOnClickPendingIntent(R.id.toggle, toggleIntent);
        remoteViews.setOnClickPendingIntent(R.id.next, nextIntent);
        remoteViews.setOnClickPendingIntent(R.id.prev, previousIntent);
        remoteViews.setOnClickPendingIntent(R.id.action_favorite, savefavIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        context.startService(new Intent(context, MusicXService.class));
        Intent updateIntent = new Intent();
        updateIntent.setAction(ACTION_COMMAND);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
        return (appWidgetIds.length > 0);
    }

    public void notifyChange(MusicXService musicXService, String what) {
        if (hasInstances(musicXService)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(musicXService);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(musicXService, this.getClass()));
            if (appWidgetIds.length > 0){
                musicxWidgetUpdate(musicXService, appWidgetIds);
                RemoteViews remoteViews = new RemoteViews(musicXService.getPackageName(), R.layout.widget);
                if (PLAYSTATE_CHANGED.equals(what)){
                    if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                        remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_pause);
                    } else {
                        remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_play);
                    }
                }
                if (META_CHANGED.equals(what)){
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           ArtworkUtils.ArtworkLoaderBitmapPalette(musicXService, musicXService.getsongAlbumName(), musicXService.getsongAlbumID(), new palette() {
                               @Override
                               public void palettework(Palette palette) {
                                   int colors[] = Helper.getAvailableColor(musicXService, palette);
                                   remoteViews.setInt(R.id.item_view, "setBackgroundColor", colors[0]);
                                   remoteViews.setInt(R.id.title, "setTextColor", Color.WHITE);
                                   remoteViews.setInt(R.id.artist, "setTextColor", Color.WHITE);
                               }
                           }, new bitmap() {
                               @Override
                               public void bitmapwork(Bitmap bitmap) {
                                   remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                                   appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
                               }

                               @Override
                               public void bitmapfailed(Bitmap bitmap) {
                                   remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                                   appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
                               }
                           });

                       }
                   });
                    FavHelper favHelper = new FavHelper(musicXService);
                    if (favHelper.isFavorite(Extras.getInstance().getSongId(musicXService.getsongId()))) {
                        remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite);
                    } else {
                        remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite_outline);
                    }
                }
                pushUpdate(musicXService, appWidgetIds, remoteViews);
            }

        }
    }
}
