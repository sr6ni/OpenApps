package id.sr.open.apps;

import android.widget.FrameLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.AbsListView;
import android.os.Handler;

public class FastScroller extends FrameLayout {

    private View thumb;
    private ListView listView;
    
    private final Handler handler = new Handler();
    private final Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.fast_scroll_view, this, true);
        thumb = findViewById(R.id.scroll_thumb);

        thumb.setOnTouchListener(new View.OnTouchListener() {
                float thumbOffset;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float y = event.getY();
                    show();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            thumb.setBackgroundResource(R.drawable.thumb_pressed);
                            thumbOffset = y - thumb.getY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float newY = y - thumbOffset;
                            moveThumb(newY);
                            scrollListToThumb();
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            thumb.setBackgroundResource(R.drawable.thumb_default);
                            return true;
                    }
                    return false;
                }
            });
    }

    private void moveThumb(float y) {
        float max = getHeight() - thumb.getHeight();
        y = Math.max(0, Math.min(max, y));
        thumb.setY(y);
    }

    private void scrollListToThumb() {
        if (listView != null && listView.getAdapter() != null) {
            float proportion = thumb.getY() / (getHeight() - thumb.getHeight());
            int itemCount = listView.getAdapter().getCount();
            int target = (int) (proportion * itemCount);
            listView.setSelection(target);
        }
    }

    public void attachListView(final ListView lv) {
        this.listView = lv;

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) { }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    show();
                    updateThumb(firstVisibleItem, totalItemCount);
                }
            });
    }

    private void updateThumb(int firstVisibleItem, int totalItemCount) {
        if (totalItemCount == 0) return;
        float proportion = (float) firstVisibleItem / (float) (totalItemCount - 0);
        float thumbY = proportion * (getHeight() - thumb.getHeight());
        thumb.setY(thumbY);
    }
    private void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            setAlpha(0f);
            setTranslationX(getWidth());
            animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(200)
                .start();
        }
        handler.removeCallbacks(hideRunnable);
        handler.postDelayed(hideRunnable, 1500);
    }

    private void hide() {
        animate()
            .translationX(getWidth())
            .alpha(0f)
            .setDuration(200)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.INVISIBLE);
                    setAlpha(1f);
                    setTranslationX(0);
                }
            })
            .start();
    }
}

