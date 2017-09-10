package ru.maxost.vk_superior_post.Utils.KeyboardHeight;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.maxost.vk_superior_post.R;

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
public class KeyboardHeightActivity extends AppCompatActivity implements KeyboardHeightObserver {

    /** Tag for logging */
    private final static String TAG = "sample_MainActivity";

    /** The keyboard height provider */
    protected KeyboardHeightProvider keyboardHeightProvider;

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        //no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        keyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.close();
    }
}