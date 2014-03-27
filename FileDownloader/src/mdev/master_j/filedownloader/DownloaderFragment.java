package mdev.master_j.filedownloader;

import java.io.File;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DownloaderFragment extends Fragment {
	private Button actionButton;
	private TextView statusTextView;
	private boolean downloading;
	private boolean downloaded;

	private OnClickListener actionButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!downloaded) {
				new PictureDownloaderAsyncTask().execute();
			} else {
				// show image
			}
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
			downloading = true;
			downloaded = false;
			updUI();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			String albumName = getString(R.string.name_local_album);

			File albumDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
			if (!albumDirectory.canRead()) {
				Log.d("mj_tag", "Can't read from " + albumDirectory.getAbsolutePath());
				downloading = false;
				downloaded = false;
				updUI();
				return;
			}

			String picureName = getString(R.string.name_local_picture);

			File picFile = new File(albumDirectory.getAbsolutePath() + "/" + picureName);
			if (!picFile.exists()) {
				Log.d("mj_tag", "Can't find picture at " + picFile.getAbsolutePath());
				downloading = false;
				downloaded = false;
				updUI();
				return;
			}

			Uri uri = Uri.fromFile(picFile);

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			// intent.setDataAndType(uri, "image/jpg");
			intent.setData(uri);
			startActivity(intent);

			downloading = false;
			downloaded = true;
			updUI();
		}
	}
}