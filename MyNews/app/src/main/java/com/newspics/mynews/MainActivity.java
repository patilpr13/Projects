package com.newspics.mynews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NCPrefFile";

    public static final int TEXT = 0;
    public static final int VIDEO = 1;
    public static final int IMAGE = 2;
    int activityToOpen;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private int NUM_PAGES = 0;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private static VerticalViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private static PagerAdapter mPagerAdapter;

    private ActionBar actionBar;

    private FloatingActionButton floatingActionButton;

    private int currentItem;

    private int backButtonCount;

    Intent intent;

    private static News newsObj = null;

    private Toolbar toolbar;

    int mToolbarHeight, mAnimDuration = 200/* milliseconds */;

    ValueAnimator mVaActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NUM_PAGES = newsObj.getNumberOfPages();
        intent = new Intent(MainActivity.this, DetailsActivity.class);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (VerticalViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
       // mPager.setRotation(90);
        mPager.setBackgroundColor(Color.parseColor("#071817"));
        mPager.setPageTransformer(false, new FadePageTransformer());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setShowHideAnimationEnabled(true);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openActivity();
            }
        });

        floatingActionButton.setVisibility(View.GONE);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                backButtonCount = 0;
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new OnSwipeListener() {
            @Override
            public boolean onSwipe(Direction direction) {
                switch (direction) {
                    case left:
                        openActivity();
                        break;
                    case right:
                        hideActionBar();
                        drawer.openDrawer(GravityCompat.START);
                        break;
                    case up:
                    case down:
                        hideActionBar();
                        break;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(actionBar.isShowing()){
                    hideActionBar();
                }
                else {
                    showActionBar();
                }
                return super.onSingleTapConfirmed(e);

            }
        });
        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event))
                    return true;
                else
                    return false;
            }
        });

        VerticalViewPager.SimpleOnPageChangeListener pageChangeListener= new VerticalViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                backButtonCount = 0;
                activityToOpen = newsObj.getPages().get(position).getType();
                switch (activityToOpen){
                    case TEXT:
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_details));
                        break;
                    case VIDEO:
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_go));
                        break;
                    case IMAGE:
                        floatingActionButton.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        };

        mPager.setOnPageChangeListener(pageChangeListener);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int prevCard = settings.getInt("PREV_CARD", 0);
        mPager.setCurrentItem(prevCard, true);
        if(prevCard == 0){
            pageChangeListener.onPageSelected(prevCard);
        }
    }

    public void termsOfUse(View view) {

    }

    /**
     * Refresh pager.
     * -1 => all download completed.
     */
    public static void refreshPager(int pageNumber) {
        if(mPager != null && mPagerAdapter != null &&  (pageNumber == mPager.getCurrentItem() || pageNumber == -1)) {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Set the News object containing all data.
     * @param news The News object.
     */
    public static void setNewsObject(News news){
        newsObj = news;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("PREV_CARD", mPager.getCurrentItem());

        // Commit the edits!
        editor.commit();

    }

    @Override
    public void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    private void openDetailsActivity(int type){
        backButtonCount = 0;
        if(currentItem != mPager.getCurrentItem()){
            if(DetailsActivity.detailsActivity != null){
                DetailsActivity.detailsActivity.finish();
            }
            currentItem = mPager.getCurrentItem();
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        DetailsActivity.setCurrentCard(String.valueOf(mPager.getCurrentItem()));
        DetailsActivity.setNewsType(type);
        hideActionBar();
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void openActivity(){
        switch (activityToOpen){
            case TEXT:
                openDetailsActivity(TEXT);
                break;
            case VIDEO:
                openDetailsActivity(VIDEO);
                break;
            case IMAGE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            if(backButtonCount >= 1)
            {
                backButtonCount = 0;
                if(DetailsActivity.detailsActivity != null){
                    DetailsActivity.detailsActivity.finish();
                }
                finish();
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this, "Press back once again to close!", Toast.LENGTH_SHORT).show();
                backButtonCount++;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
       if (id == R.id.home_action) {
            mPager.setCurrentItem(0,true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private class FadePageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageHeight = view.getHeight();
             if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setTranslationY(0);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);
                view.setTranslationY(pageHeight * -position);
            }
        }

    }

   private  void hideActionBar() {
        // initialize `mToolbarHeight`
        if (mToolbarHeight == 0) {
            mToolbarHeight = toolbar.getHeight();
        }

        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // animate `Toolbar's` height to zero.
        mVaActionBar = ValueAnimator.ofInt(mToolbarHeight , 0);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                ((AppBarLayout.LayoutParams)toolbar.getLayoutParams()).height
                        = (Integer)animation.getAnimatedValue();
                toolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (getSupportActionBar() != null) { // sanity check
                    getSupportActionBar().hide();
                }
            }
        });

        mVaActionBar.setDuration(mAnimDuration);
        mVaActionBar.start();
    }

    private void showActionBar() {
        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // restore `Toolbar's` height
        mVaActionBar = ValueAnimator.ofInt(0 , mToolbarHeight);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                ((AppBarLayout.LayoutParams)toolbar.getLayoutParams()).height
                        = (Integer)animation.getAnimatedValue();
                toolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if (getSupportActionBar() != null) { // sanity check
                    getSupportActionBar().show();
                }
            }
        });

        mVaActionBar.setDuration(mAnimDuration);
        mVaActionBar.start();
    }
}
