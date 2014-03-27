package mdev.master_j.filedownloader;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class DownloaderFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button b = (Button) getActivity().findViewById(R.id.button_action);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onActionButtonClick();
			}
		});
	}

	private void onActionButtonClick() {
		String url = "http://developer.android.com/design/media/new_widgets.png";
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			new Downloader().execute(url);
		} else {
			Toast.makeText(getActivity(), "no connection", Toast.LENGTH_SHORT).show();
		}
	}

	private class Downloader extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... urls) {
			return downloadBitmap(urls[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView img = (ImageView) getActivity().findViewById(R.id.imageView1);

		}
	}

}