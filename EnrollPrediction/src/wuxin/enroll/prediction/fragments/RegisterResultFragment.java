package wuxin.enroll.prediction.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import wuxin.enroll.prediction.R;

public class RegisterResultFragment extends Fragment {
	private TextView mNumber;
	private String account = null;
	private Button mRegister;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View root = inflater.inflate(R.layout.fragment_register_result, container, false);
    	mNumber = (TextView)root.findViewById(R.id.account_number);
    	account = this.getActivity().getIntent().getStringExtra("account");
    	if (account != null){
    		mNumber.setText("’À∫≈£∫" + account);
    	}
    	mRegister = (Button) root.findViewById(R.id.register_success);
    	mRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("wuxin.enroll.prediction.login.LoginActivity");
				intent.putExtra("account", account);
				startActivity(intent);
			}
		});
        return root;
    }
}
