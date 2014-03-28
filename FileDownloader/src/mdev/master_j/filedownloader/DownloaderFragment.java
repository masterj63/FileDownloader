package mdev.master_j.filedownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloaderFragment extends Fragment {
	private Handler handler = new Handler();
	private Button actionButton;
	private TextView statusTextView;
	private boolean downloading;
	private boolean downloaded;

	private OnClickListener actionButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!downloaded) {
				ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
				if (netInfo == null || !netInfo.isConnected()) {
					Log.d("mj_tag", "No internet connection");
					downloading = false;
					downloaded = false;
					updUI();
					return;
				}
				new PictureDownloaderAsyncTask().execute();
				updUI();
			} else
				showPicture();
			updUI();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		downloading = false;
		downloaded = false;

		actionButton = (Button) getActivity().findViewById(R.id.button_action);
		actionButton.setOnClickListener(actionButtonClickListener);

		statusTextView = (TextView) getActivity().findViewById(R.id.status_textview);

		updUI();
	}

	private void updUI() {
		if (!downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_idle);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(true);
			return;
		}
		if (downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_loading);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(false);
			return;
		}
		if (!downloading && downloaded) {
			statusTextView.setText(R.string.status_textview_loaded);
			actionButton.setText(R.string.button_action_open);
			actionButton.setEnabled(true);
			return;
		}
		if (downloading && downloaded) {
			String text = "both downloading and downloaded are true";
			IllegalStateException exception = new IllegalStateException(text);
			Log.d("mj_tag", text, exception);
			throw exception;
		}
	}

	private class PictureDownloaderAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			String pictureUrl = getString(R.string.url_picture);
			try {
				URL url = new URL(pictureUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// init
				conn.setReadTimeout(15000);
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// start
				conn.connect();
				int response = conn.getResponseCode();
				Log.d("mj_tag", "The response is: " + response);
				// save
				InputStream inStream = conn.getInputStream();

				File albumDirectory = getAlbumDirectory();
				if (!albumDirectory.mkdirs() && !albumDirectory.exists()) {
					Log.d("mj_tag", "Cannot access " + albumDirectory.getAbsolutePath());
					downloading = false;
					downloaded = false;
					return null;
				}
				downloading = true;
				downloaded = false;
				handler.post(new Runnable() {
					@Override
					public void run() {
						updUI();
					}
				});

				String pictureName = getString(R.string.name_local_picture);
				File pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + pictureName);
				OutputStream outStream = new FileOutputStream(pictureFile);

				int loaded = 0;
				int total = conn.getContentLength();
				int t;
				while ((t = inStream.read()) != -1) {
					loaded++;
					outStream.write(t);

					Activity a = getActivity();
					if (a == null)
						continue;

					View v = a.findViewById(R.id.download_progressbar);
					if (v == null)
						continue;

					ProgressBar pb = (ProgressBar) v;

					pb.setMax(total);
					pb.setProgress(loaded);
				}
				outStream.flush();
				outStream.close();
			} catch (MalformedURLException e) {
				Log.d("mj_tag", "MalformedURLException", e);
				e.printStackTrace();
			} catch (ProtocolException e) {
				Log.d("mj_tag", "ProtocolException", e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("mj_tag", "ProtocolException", e);
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			downloading = false;
			downloaded = true;
			updUI();
		}
	}

	private void showPicture() {
		File albumDirectory = getAlbumDirectory();
		if (!albumDirectory.canRead()) {
			Log.d("mj_tag", "Can't read from " + albumDirectory.getAbsolutePath());
			downloading = false;
			downloaded = false;
			updUI();
			return;
		}

		String picureName = getString(R.string.name_local_picture);

		File pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + picureName);
		if (!pictureFile.exists()) {
			Log.d("mj_tag", "Can't find picture at " + pictureFile.getAbsolutePath());
			downloading = false;
			downloaded = false;
			updUI();
			return;
		}

		Uri uri = Uri.fromFile(pictureFile);

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}

	private File getAlbumDirectory() {
		String albumName = getString(R.string.name_local_album);
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
	}
}