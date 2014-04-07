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
import android.widget.Toast;

public class DownloaderFragment extends Fragment {
	private static final int BUFFER_SIZE_BYTES = 2048;

	private Handler handler = new Handler();
	private Button actionButton;
	private TextView statusTextView;
	private ProgressBar downloadProgressBar;
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
					Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
					downloading = false;
					downloaded = false;
					updateUI();
					return;
				}
				new PictureDownloaderAsyncTask().execute();
				updateUI();
			} else
				showPicture();
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

		actionButton = (Button) getActivity().findViewById(R.id.button_action);
		actionButton.setOnClickListener(actionButtonClickListener);

		statusTextView = (TextView) getActivity().findViewById(R.id.status_textview);

		downloadProgressBar = (ProgressBar) getActivity().findViewById(R.id.download_progressbar);
	}

	@Override
	public void onResume() {
		super.onResume();
		updateUI();
	}

	private void updateUI() {
		if (!downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_idle);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(true);
			downloadProgressBar.setVisibility(View.INVISIBLE);
			return;
		}
		if (downloading && !downloaded) {
			statusTextView.setText(R.string.status_textview_loading);
			actionButton.setText(R.string.button_action_download);
			actionButton.setEnabled(false);
			downloadProgressBar.setVisibility(View.VISIBLE);
			return;
		}
		if (!downloading && downloaded) {
			statusTextView.setText(R.string.status_textview_loaded);
			actionButton.setText(R.string.button_action_open);
			actionButton.setEnabled(true);
			downloadProgressBar.setVisibility(View.INVISIBLE);
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
			int loaded = 0;
			int total = 0;
			String pictureUrl = getString(R.string.url_picture);
			File pictureFile = null;
			try {
				URL url = new URL(pictureUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// init
				conn.setReadTimeout(5000);
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
					toastText("Cannot access " + albumDirectory.getAbsolutePath());
					downloading = false;
					downloaded = false;
					return null;
				}
				downloading = true;
				downloaded = false;
				handler.post(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});

				String pictureName = getString(R.string.name_local_picture);
				pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + pictureName);
				OutputStream outStream = new FileOutputStream(pictureFile);

				loaded = 0;
				total = conn.getContentLength();
				byte buffer[] = new byte[BUFFER_SIZE_BYTES];
				int bytesRead;
				while ((bytesRead = inStream.read(buffer)) != -1) {
					loaded += bytesRead;
					outStream.write(buffer, 0, bytesRead);

					downloadProgressBar.setMax(total);
					downloadProgressBar.setProgress(loaded);
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

			if (loaded != total) {
				toastText("downloading error");
				downloading = false;
				downloaded = false;
			} else {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
				intent.setData(Uri.fromFile(pictureFile));
				getActivity().sendBroadcast(intent);

				downloading = false;
				downloaded = true;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			updateUI();
		}
	}

	private void showPicture() {
		File albumDirectory = getAlbumDirectory();
		if (!albumDirectory.canRead()) {
			Log.d("mj_tag", "Can't read from " + albumDirectory.getAbsolutePath());
			Toast.makeText(getActivity(), "Can't read from " + albumDirectory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			downloading = false;
			downloaded = false;
			updateUI();
			return;
		}

		String picureName = getString(R.string.name_local_picture);

		File pictureFile = new File(albumDirectory.getAbsolutePath() + "/" + picureName);
		if (!pictureFile.exists()) {
			Log.d("mj_tag", "Can't find picture at " + pictureFile.getAbsolutePath());
			Toast.makeText(getActivity(), "Can't find picture at " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
			downloading = false;
			downloaded = false;
			updateUI();
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

	private void toastText(final String text) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
			}
		});
	}
}