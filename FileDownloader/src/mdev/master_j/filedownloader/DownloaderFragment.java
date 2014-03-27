package mdev.master_j.filedownloader;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
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

			downloading = false;
			downloaded = true;
			updUI();
		}
	}
}