package wuxin.enroll.prediction.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import wuxin.enroll.prediction.R;

public class RegisterFregment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_register, container, false);
        Button register = (Button) root.findViewById(R.id.register);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	startActivity(new Intent("wuxin.enroll.prediction.login.RegisterPhoneActivity"));
            }
        });
        return root;
    }
}
