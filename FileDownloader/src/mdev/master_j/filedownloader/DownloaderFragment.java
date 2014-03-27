package mdev.master_j.filedownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class DownloaderFragment extends Fragment {
	private static final String PICTURE_NAME = "pic.jpg";

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
		Log.d("mj>>>", "clicked");
		// String url =
		// "http://www.freedomwallpaper.com//nature-wallpaper-hd/6.jpg";
		String url = "http://developer.android.com/images/home/kk-hero.jpg";
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			new Downloader().execute(url);
		} else {
			Toast.makeText(getActivity(), "no connection", Toast.LENGTH_SHORT).show();
		}
	}

	private class Downloader extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... urls) {
			downloadBitmap(urls[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			super.onPostExecute(v);

			File file_t = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mdev");
			if (!file_t.mkdirs()) {
				Log.d("mj>>>", "Directory not created");
			}

			File galDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mdev");
			File picFile = new File(galDir.getAbsoluteFile() + "/" + PICTURE_NAME);

			Log.d("mj>>>", picFile.getAbsolutePath());
			Log.d("mj>>>", "pic exists : " + picFile.exists());

			if (!picFile.exists()) {
				Toast.makeText(getActivity(), "no pic found", Toast.LENGTH_SHORT).show();
				return;
			}

			Uri uri = Uri.fromFile(picFile);
			Log.i("mj>>>", picFile.getAbsolutePath());
			Log.i("mj>>>", uri.getPath());

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "image/jpg");
			startActivity(intent);

			Log.d("mj>>>", "opened");
		}
	}

	private Bitmap downloadBitmap(String picUrl) {
		InputStream ins = null;

		try {
			URL url = new URL(picUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// init
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// start
			conn.connect();
			int response = conn.getResponseCode();
			Log.d("mj>>>", "The response is: " + response);
			ins = conn.getInputStream();
			// save

			File galDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mdev");
			File picFile = new File(galDir.getAbsolutePath() + "/" + PICTURE_NAME);
			Log.d("mj>>>", "streaming file to : " + picFile.getAbsolutePath() + " ;  exists : " + picFile.exists());
			OutputStream ous = new FileOutputStream(picFile);
			int t;
			while ((t = ins.read()) != -1)
				ous.write(t);
			ous.flush();
			ous.close();
			Log.d("mj>>>", "saved; ous closed.");
			Log.d("mj>>>", "streamed file to : " + picFile.getAbsolutePath() + " ;  exists : " + picFile.exists());
		} catch (MalformedURLException e) {
			Log.d("mj>>>", "MalformedURLException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("mj>>>", "IOException");
			for (StackTraceElement s : e.getStackTrace())
				Log.d("mj>>>", ">" + s.toString());
			e.printStackTrace();
		}
		return null;
	}
}