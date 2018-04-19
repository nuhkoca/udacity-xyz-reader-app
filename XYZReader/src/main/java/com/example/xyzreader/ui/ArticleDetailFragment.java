package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.ENGLISH);
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.

        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().initLoader(1, null, this);
        }
    }

    @SuppressLint("PrivateResource")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        final TextView articleTitleLand = mRootView.findViewById(R.id.article_title_land);
        TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = mRootView.findViewById(R.id.article_body);
        ImageView mPhotoView = mRootView.findViewById(R.id.poster);
        final CardView cvDetailLand = mRootView.findViewById(R.id.cvDetailLand);

        if (getActivity() != null) {
            bodyView.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.Roboto_Regular));
            bylineView.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.Roboto_Medium));
            articleTitleLand.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.Roboto_Regular));
        }

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            if (getResources().getBoolean(R.bool.isLand)) {
                articleTitleLand.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            }

            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));

            }
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            if (getResources().getBoolean(R.bool.isLand)) {
                prepareImage(cvDetailLand, articleTitleLand, mPhotoView);
            }
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().restartLoader(1, null, this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().destroyLoader(1);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

    private void prepareImage(final CardView cvDetailLand, final TextView articleTitleLand, ImageView mPhotoView) {
        if (getActivity() != null) {
            Glide.with(getActivity().getApplicationContext())
                    .asBitmap()
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    if (palette.getDominantSwatch() != null) {
                                        cvDetailLand.setCardBackgroundColor(palette.getDominantSwatch().getRgb());
                                        articleTitleLand.setTextColor(palette.getDominantSwatch().getBodyTextColor());
                                    }
                                }
                            });

                            return false;
                        }
                    })
                    .into(mPhotoView);
        }
    }
}