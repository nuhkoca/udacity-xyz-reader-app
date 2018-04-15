package com.example.xyzreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.Toast;

import com.example.xyzreader.R;
import com.example.xyzreader.adapter.Adapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private long mBackPressed;

    private static Parcelable mLayoutManagerState;
    private static final String LAYOUT_MANAGER_STATE = "LAYOUT_MANAGER_STATE";

    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        setupSwipe();

        mRecyclerView = findViewById(R.id.recycler_view);

        getSupportLoaderManager().initLoader(0, null, this);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
         staggeredGridLayoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        if (savedInstanceState == null) {
            refresh();
        } else {
            mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManagerState);
        }

    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    private void setupSwipe(){
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipeColor1,
                R.color.swipeColor2,
                R.color.swipeColor3);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);

        getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    public void onBackPressed() {
        int timeDelay = 1500;

        if (mBackPressed + timeDelay > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.twice_press_to_exit),
                    Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLayoutManagerState = mRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LAYOUT_MANAGER_STATE, mLayoutManagerState);

        super.onSaveInstanceState(outState);
    }
}