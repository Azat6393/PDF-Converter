package woynapp.wsann.fragment.new_fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import woynapp.wsann.R;
import woynapp.wsann.activity.NewMainActivity;
import woynapp.wsann.activity.WebViewActivity;

public class PopUpDialog extends DialogFragment {

    VideoView videoView;
    ImageView close;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pop_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoView = view.findViewById(R.id.videoview);
        close = view.findViewById(R.id.close);

        Uri uri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.video);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.stopPlayback();
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                startActivity(intent);
                getDialog().dismiss();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        videoView.resume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        videoView.stopPlayback();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }
}
