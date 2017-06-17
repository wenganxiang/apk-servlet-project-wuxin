package wuxin.enroll.prediction.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import wuxin.enroll.prediction.CommonListAdapter;
import wuxin.enroll.prediction.CommonListItem;
import wuxin.enroll.prediction.R;
import wuxin.enroll.prediction.utils.LoginAccount;

public class MeFragment extends Fragment implements OnClickListener, OnItemClickListener {

    private final static String favourite = "favourite";
    private final static String becomeVIP = "becomeVIP";
    private final static String settings = "settings";
    private final static String aidCenter = "aidCenter";
    private final static String share = "share";
    private final static String about = "about";

    private Context mContext;
    private ListView mList;
    private CommonListAdapter mAdapter;
    private ArrayList<CommonListItem> mListItems;
    private Button mLogin;
    private Button mRegister;
    private TextView loginAlert;
    private TextView remainDays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getContext();
        View root = inflater.inflate(R.layout.fragment_main_3rd, container, false);
        initListItems();
        mList = (ListView) root.findViewById(R.id.me_comm_list);
        mAdapter = new CommonListAdapter(mContext, mListItems);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        RelativeLayout icon = (RelativeLayout) root.findViewById(R.id.topItem1);
        icon.setVisibility(View.INVISIBLE);
        TextView title = (TextView) root.findViewById(R.id.topText2);
        title.setText("�ҵ�");
        mLogin = (Button) root.findViewById(R.id.loginBtn);
        mLogin.setOnClickListener(this);
        mRegister = (Button) root.findViewById(R.id.registerBtn);
        mRegister.setOnClickListener(this);
        loginAlert = (TextView) root.findViewById(R.id.loginAlert);
        remainDays =  (TextView) root.findViewById(R.id.gaokaoLimit);
        // 1496764800 Ϊ2017.06.07 00:00:00��ʱ���
        int remain = (int)((1496764800 - System.currentTimeMillis() / 1000) / 3600 /24) ;
        remainDays.setText("����߿���ʣ " + remain + " �죨�߿�ʱ��:2017.6.7��");
        return root;
    }

    @Override
    public void onResume(){
    	super.onResume();
    	String account =LoginAccount.getInstance().getAccount();
    	if (account == null) {
    		loginAlert.setText("��½��ȡ���๦��");
    		mLogin.setText("��½");
    		mRegister.setVisibility(View.VISIBLE);
    	} else {
    		loginAlert.setText("��ǰ�˺�:" + account);
    		mLogin.setText("�л��˺�");
    		mRegister.setVisibility(View.GONE);
    	}
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CommonListItem item = mListItems.get(position);
        if (item == null)
            return;
        if (item.onClickKey == null)
            return;
        Toast.makeText(getContext(), "���ܴ�����", Toast.LENGTH_SHORT).show();
        switch (item.onClickKey) {
        case favourite:
            break;
        case becomeVIP:
            break;
        case settings:
            break;
        case aidCenter:
            break;
        case share:
            break;
        case about:
            break;
        }
    }

    private void initListItems() {
        mListItems = new ArrayList<>();
        CommonListItem item;
        // item -1 �ָ���
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 0 �ղ�
        item = new CommonListItem();
        item.leftIconId = R.drawable.favourites;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "�ղ�";
        item.text = "ѧУ��רҵ��ְҵ������";
        item.onClickKey = favourite;
        mListItems.add(item);
        // item 1 �ָ���
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 2 ����
        item = new CommonListItem();
        item.leftIconId = R.drawable.main_settings;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "����";
        item.onClickKey = settings;
        mListItems.add(item);
        // item 3 �ָ���
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 4 ��ͨVIP
        item = new CommonListItem();
        item.leftIconId = R.drawable.become_vip;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "��ͨVIP";
        item.onClickKey = becomeVIP;
        mListItems.add(item);
        // item 5�ָ���
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 6 ��������
        item = new CommonListItem();
        item.leftIconId = R.drawable.get_aid;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "��������";
        item.onClickKey = aidCenter;
        mListItems.add(item);
        // item 7 ��������
        item = new CommonListItem();
        item.leftIconId = R.drawable.about;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "��������";
        item.onClickKey = about;
        mListItems.add(item);

        // item last ���һ�����������
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
        case R.id.loginBtn:
            startActivity(new Intent("wuxin.enroll.prediction.login.LoginActivity"));
            break;
        case R.id.registerBtn:
        	startActivity(new Intent("wuxin.enroll.prediction.login.RegisterPhoneActivity"));
            break;
        }
    }
}
