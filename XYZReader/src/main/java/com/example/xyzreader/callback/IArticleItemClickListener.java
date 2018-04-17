package com.example.xyzreader.callback;

import android.widget.ImageView;

public interface IArticleItemClickListener {
    void onClick(long articleId, String title, String author, String imageUrl, ImageView thumbnail);
}
