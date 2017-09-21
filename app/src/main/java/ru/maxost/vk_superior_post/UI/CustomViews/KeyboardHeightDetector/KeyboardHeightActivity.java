package ru.maxost.vk_superior_post.UI.CustomViews.KeyboardHeightDetector;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
public class KeyboardHeightActivity extends AppCompatActivity implements KeyboardHeightObserver {

    protected KeyboardHeightProvider keyboardHeightProvider;

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        //no-op
    }


    @Override
    public void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
    }
}