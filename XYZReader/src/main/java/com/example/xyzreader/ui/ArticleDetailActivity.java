package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        if (!getResources().getBoolean(R.bool.isLand)) {
            hideStatusBarOnly();
        }

        getSupportLoaderManager().initLoader(2, null, this);

        if (getIntent() != null && getIntent().getData() != null) {
            mStartId = ItemsContract.Items.getItemId(getIntent().getData());
        }

        setupUI();
    }

    private void setupUI() {
        String title = getIntent().getStringExtra("title");
        String imageUrl = getIntent().getStringExtra("image");
        final String author = getIntent().getStringExtra("author");

        FloatingActionButton share_fab = findViewById(R.id.share_fab);
        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText(String.format(getString(R.string.action_share_text),
                                author))
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        CollapsingToolbarLayout titleView = findViewById(R.id.article_title);

        if (!getResources().getBoolean(R.bool.isLand)) {
            titleView.setTitle(title);
        }

        ImageView mPhotoView = findViewById(R.id.poster);

        if (!getResources().getBoolean(R.bool.isLand)) {
            prepareImage(mPhotoView, imageUrl);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClicked = item.getItemId();

        switch (itemThatWasClicked) {
            case android.R.id.home:
                setResult(RESULT_OK);
                onBackPressed();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursor = data;

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();

            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    break;
                }
                mCursor.moveToNext();

            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_article_detail, ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID)))
                    .commit();


            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursor = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(2, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        getSupportLoaderManager().destroyLoader(2);
    }

    public void hideStatusBarOnly() {
        Window decorWindow = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            decorWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            decorWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            decorWindow.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void prepareImage(ImageView mPhotoView, String imageUrl) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .into(mPhotoView);
    }
}