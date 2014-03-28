package mdev.master_j.filedownloader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class DowloaderActivity extends ActionBarActivity {
	private static final String TAG_DOWNLOADER_FRAGMENT = "mdev.master_j.filedownloader.DownloaderFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(TAG_DOWNLOADER_FRAGMENT);
		if (fragment == null) {
			fragment = new DownloaderFragment();
			fragment.setRetainInstance(true);
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(R.id.layout_activity_main, fragment, TAG_DOWNLOADER_FRAGMENT);
			fragmentTransaction.commit();
		}
	}
}
