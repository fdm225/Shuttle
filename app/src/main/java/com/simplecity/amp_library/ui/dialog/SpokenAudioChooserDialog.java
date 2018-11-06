package com.simplecity.amp_library.ui.dialog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.simplecity.amp_library.R;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;


public class SpokenAudioChooserDialog {

    private static final String TAG = "SpokenAudioChooserDialog";

    private SpokenAudioChooserDialog() {
        //no instance
    }

    private static void setSelectedGenres(SharedPreferences sharedPreferences, String type,
                                          String lv_items[], ListView lView) {

//        Log.d("fdm225", "SpokenAudioChooserDialog - setSelectedGenres: genres:" +
//                sharedPreferences.getString("audiobook_genres", ""));

        String genres[] =
                sharedPreferences.getString(type+"_genres", "").split(",");

        for (String genre : genres) {
            int j = 0;
            for (int i = j; i < lv_items.length; i++) {
                if (genre.equals(lv_items[i])) {
//                    Log.d("fdm225", "SpokenAudioChooserDialog - setSelectedGenres: found:"
//                            + genre);
                    lView.setItemChecked(i, true);
                    j = i + 1;
                    break;
                }
            }
        }
    }

    private static String getSelectedGenres(int lv_items_count, ListView lView) {

        StringBuilder sb = new StringBuilder();
        SparseBooleanArray checked = lView.getCheckedItemPositions();
//        Log.d("fdm225", "number checked: " + checked.size());
        for (int i = 0; i <  lv_items_count; i++) {
//            Log.d("fdm225", "i=" + i);
            if (checked.get(i)) {
//                Log.d("fdm225", "getSelectedGenres checked: " +
//                        (String) lView.getItemAtPosition(i) + ":" + i);

                sb.append((String) lView.getItemAtPosition(i));
                sb.append(",");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

//        Log.d("fdm225", "getSelectedGenres: " + sb.toString());
        return sb.toString();

    }

    private static String[] getGenresFromMediaStore(Activity activity) {
        String lv_items[] = null;
        String idColumn = "_id";
        String nameColumn = MediaStore.Audio.GenresColumns.NAME;

        Cursor cursor = activity.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                null, null, null, nameColumn);

        if (cursor.getCount() > 0) {
            ArrayList<String> genreList = new ArrayList<String>();
            int genreIdColIndex = cursor.getColumnIndex(idColumn);
            int genreNameIndex = cursor.getColumnIndex(nameColumn);

            while (cursor.moveToNext()) {

                //Log.d("fdm225","genreIdIndex:" + genreIdColIndex);
                //Log.d("fdm225","genreNameIndex:" + genreNameIndex);
                Long genreId = cursor.getLong(genreIdColIndex);

                Cursor membersCursor = activity.getContentResolver().query(
                        MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                        null, null, null, null);
                if (membersCursor.getCount() > 0
                        && !genreList.contains(cursor.getString(genreNameIndex))) {
                    genreList.add(cursor.getString(genreNameIndex));
                    //Log.d("fdm225", cursor.getString(genreNameIndex) + ":" + membersCursor.getCount());
                }
                membersCursor.close();
            }
            if (genreList.size() > 0) {
                lv_items = new String[genreList.size()];
                genreList.toArray(lv_items);
            } else {
                lv_items = new String[]{"No Genres Found"};
            }
        }
        return lv_items;
    }

    private static void writeGenresToPreferences(SharedPreferences sharedPreferences, String type,
                                                 int lv_items_count, ListView lView) {
        String genres = getSelectedGenres(lv_items_count, lView);
//        Log.d("fdm225", "SpokenAudioChooserDialog - onPause: genres:"
//                + genres);

        Editor editor = sharedPreferences.edit();
        editor.putString(type+"_genres", genres);
        editor.apply();
    }


    public static MaterialDialog getDialog(Activity activity, String type) {

        ListView lView = new ListView(activity);
        String lv_items[] = new String[]{"No Genres Found"};
        String title = "";

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        if(type.equals("audiobook")) {
            title = activity.getResources().getString(R.string.audiobook_select_genre_title);
        }
        else {
            title = activity.getResources().getString(R.string.podcast_select_genre_title);
        }

        // Set option as Multiple Choice. So that user can able to select more the one option from list
        lView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lv_items = getGenresFromMediaStore(activity);

        lView.setAdapter(new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_multiple_choice,
                lv_items));

        setSelectedGenres(sharedPreferences, type, lv_items, lView);

        int lv_items_count = lv_items.length;
        return new MaterialDialog.Builder(activity)
                .title(title)
                .customView(lView, false)
                .positiveText(R.string.button_done)
                .onPositive((dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    writeGenresToPreferences(sharedPreferences, type, lv_items_count,  lView);

                })
                .negativeText(R.string.close)
                .build();
    }
}
