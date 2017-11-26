/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.asynctaskloader;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.asynctaskloader.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

// (1) implement LoaderManager.LoaderCallbacks<String> on MainActivity
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /* A constant to save and restore the URL that is being displayed */
    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    // (28) Remove the key for storing the search results JSON
    /* A constant to save and restore the JSON that is being displayed */
//    private static final String SEARCH_RESULTS_RAW_JSON = "results";

    // (2) Create a constant int to uniquely identify your loader. Call it GITHUB_SEARCH_LOADER
    private static final int GITHUB_SEARCH_LOADER = 22;

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);

            // (26) Remove the code that retrieves the JSON
//            String rawJsonSearchResults = savedInstanceState.getString(SEARCH_RESULTS_RAW_JSON);

            mUrlDisplayTextView.setText(queryUrl);
            // (25) Remove the code that displays the JSON
//            mSearchResultsTextView.setText(rawJsonSearchResults);
        }

        // (24) Initialize the loader with GITHUB_SEARCH_LOADER as the ID, null for the bundle, and this for the context
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(GITHUB_SEARCH_LOADER, null, this);
    }

    /**
     * This method retrieves the search text from the EditText, constructs the
     * URL (using {@link NetworkUtils}) for the github repository you'd like to find, displays
     * that URL in a TextView, and finally request that an AsyncTaskLoader performs the GET request.
     */
    private void makeGithubSearchQuery() {
        String githubQuery = mSearchBoxEditText.getText().toString();

        // (17) If no search was entered, indicate that there isn't anything to search for and return
        if (TextUtils.isEmpty(githubQuery)) {
            return;
        }

        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());

        // (18) Remove the call to execute the AsyncTask
//        new GithubQueryTask().execute(githubSearchUrl);

        // (19) Create a bundle called queryBundle
        // (20) Use putString with SEARCH_QUERY_URL_EXTRA as the key and the String value of the URL as the value
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, githubSearchUrl.toString());

        // (21) Call getSupportLoaderManager and store it in a LoaderManager variable
        // (22) Get our Loader by calling getLoader and passing the ID we specified
        // (23) If the Loader was null, initialize it. Else, restart it.
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER, queryBundle, this);
        }
    }

    /**
     * This method will make the View for the JSON data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showJsonDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the JSON data is visible */
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            /**
             * Subclasses must implement this to take care of loading their data,
             * as per {@link #startLoading()}.  This is not called by clients directly,
             * but as a result of a call to {@link #startLoading()}.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                // (6) If args is null, return.
                if (args == null) {
                    return;
                }

                // (7) Show the loading indicator
                mLoadingIndicator.setVisibility(View.VISIBLE);

                // (8) Force a load
                forceLoad();
            }

            /**
             * Called on a worker thread to perform the actual load and to return
             * the result of the load operation.
             * <p>
             * Implementations should not deliver the result directly, but should return them
             * from this method, which will eventually end up calling {@link #deliverResult} on
             * the UI thread.  If implementations need to process the results on the UI thread
             * they may override {@link #deliverResult} and do so there.
             * <p>
             * To support cancellation, this method should periodically check the value of
             * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
             * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
             * directly instead of polling {@link #isLoadInBackgroundCanceled}.
             * <p>
             * When the load is canceled, this method may either return normally or throw
             * {@link OperationCanceledException}.  In either case, the {@link Loader} will
             * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
             * result object, if any.
             *
             * @return The result of the load operation.
             * @throws OperationCanceledException if the load is canceled during execution.
             * @see #isLoadInBackgroundCanceled
             * @see #cancelLoadInBackground
             * @see #onCanceled
             */
            @Override
            public String loadInBackground() {
                // (10) Get the String for our URL from the bundle passed to onCreateLoader
                String searchUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);

                // (11) If the URL is null or empty, return null
                if (TextUtils.isEmpty(searchUrlString)) {
                    return null;
                }

                // (12) Copy the try / catch block from the AsyncTask's doInBackground method
                String githubSearchResults = null;
                try {
                    URL searchUrl = new URL(searchUrlString);
                    githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return githubSearchResults;
            }
        };
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // (14) Hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        // (15) Use the same logic used in onPostExecute to show the data or the error message
        if (data != null && !data.equals("")) {
            showJsonDataView();
            mSearchResultsTextView.setText(data);
        } else {
            showErrorMessage();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    // (3) Override onCreateLoader
    // Within onCreateLoader
        // (4) Return a new AsyncTaskLoader<String> as an anonymous inner class with this as the constructor's parameter
            // (5) Override onStartLoading
                // Within onStartLoading

                // (6) If args is null, return.

                // (7) Show the loading indicator

                // (8) Force a load
                // END - onStartLoading

            // (9) Override loadInBackground

                // Within loadInBackground
                // (10) Get the String for our URL from the bundle passed to onCreateLoader

                // (11) If the URL is null or empty, return null

                // (12) Copy the try / catch block from the AsyncTask's doInBackground method
                // END - loadInBackground

    // (13) Override onLoadFinished

        // Within onLoadFinished
        // (14) Hide the loading indicator

        // (15) Use the same logic used in onPostExecute to show the data or the error message
        // END - onLoadFinished

    // (16) Override onLoaderReset as it is part of the interface we implement, but don't do anything in this method

    // (29) Delete the AsyncTask class
//    public class GithubQueryTask extends AsyncTask<URL, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mLoadingIndicator.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected String doInBackground(URL... params) {
//            URL searchUrl = params[0];
//            String githubSearchResults = null;
//            try {
//                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return githubSearchResults;
//        }
//
//        @Override
//        protected void onPostExecute(String githubSearchResults) {
//            mLoadingIndicator.setVisibility(View.INVISIBLE);
//            if (githubSearchResults != null && !githubSearchResults.equals("")) {
//                showJsonDataView();
//                mSearchResultsTextView.setText(githubSearchResults);
//            } else {
//                showErrorMessage();
//            }
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String queryUrl = mUrlDisplayTextView.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);

        // (27) Remove the code that persists the JSON
//        String rawJsonSearchResults = mSearchResultsTextView.getText().toString();
//        outState.putString(SEARCH_RESULTS_RAW_JSON, rawJsonSearchResults);
    }
}