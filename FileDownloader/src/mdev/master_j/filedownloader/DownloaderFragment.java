package mdev.master_j.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			public void onClick(View arg0) {
				// new Thread(new TT()).start();
				// getActivity().runOnUiThread(new TT());
				try {
					new TTT().execute(new URL("http://images.stockfreeimages.com/1024/sfi/free_10245258.jpg"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private class TTT extends AsyncTask<URL, Integer, Long> {
		@Override
		protected Long doInBackground(URL... params) {
			try {
				// URL url = new
				// URL("http://images.stockfreeimages.com/1024/sfi/free_10245258.jpg");
				URL url = params[0];
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();

				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
					Toast.makeText(getActivity(), "NOT_OK", Toast.LENGTH_SHORT).show();

				FileOutputStream fos = getActivity().openFileOutput("pic.jpg", Context.MODE_PRIVATE);
				InputStream in = new BufferedInputStream(conn.getInputStream());

				int n = conn.getContentLength();
				for (int i = 0; i < n; i++)
					fos.write(in.read());

				fos.flush();
				fos.close();
				Toast.makeText(getActivity(), "downloaded", Toast.LENGTH_SHORT).show();

				ImageView img = (ImageView) getActivity().findViewById(R.id.imageView1);
				Bitmap bm = BitmapFactory.decodeStream(getActivity().openFileInput("pic.jpg"));
				img.setImageBitmap(bm);
				Toast.makeText(getActivity(), "showed", Toast.LENGTH_SHORT).show();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}