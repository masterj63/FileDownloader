package mdev.master_j.filedownloader;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class DownloaderFragment extends Fragment {
	private Button actionButton;

	private OnClickListener actionButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			actionButton.setEnabled(false);
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
		actionButton.setText(R.string.button_action_download);
	}
}