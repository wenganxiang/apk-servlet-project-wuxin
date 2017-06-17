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
        title.setText("我的");
        mLogin = (Button) root.findViewById(R.id.loginBtn);
        mLogin.setOnClickListener(this);
        mRegister = (Button) root.findViewById(R.id.registerBtn);
        mRegister.setOnClickListener(this);
        loginAlert = (TextView) root.findViewById(R.id.loginAlert);
        remainDays =  (TextView) root.findViewById(R.id.gaokaoLimit);
        // 1496764800 为2017.06.07 00:00:00的时间戳
        int remain = (int)((1496764800 - System.currentTimeMillis() / 1000) / 3600 /24) ;
        remainDays.setText("距离高考还剩 " + remain + " 天（高考时间:2017.6.7）");
        return root;
    }

    @Override
    public void onResume(){
    	super.onResume();
    	String account =LoginAccount.getInstance().getAccount();
    	if (account == null) {
    		loginAlert.setText("登陆获取更多功能");
    		mLogin.setText("登陆");
    		mRegister.setVisibility(View.VISIBLE);
    	} else {
    		loginAlert.setText("当前账号:" + account);
    		mLogin.setText("切换账号");
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
        Toast.makeText(getContext(), "功能待开放", Toast.LENGTH_SHORT).show();
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
        // item -1 分隔栏
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 0 收藏
        item = new CommonListItem();
        item.leftIconId = R.drawable.favourites;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "收藏";
        item.text = "学校、专业、职业、题库等";
        item.onClickKey = favourite;
        mListItems.add(item);
        // item 1 分隔栏
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 2 设置
        item = new CommonListItem();
        item.leftIconId = R.drawable.main_settings;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "设置";
        item.onClickKey = settings;
        mListItems.add(item);
        // item 3 分隔栏
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 4 开通VIP
        item = new CommonListItem();
        item.leftIconId = R.drawable.become_vip;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "开通VIP";
        item.onClickKey = becomeVIP;
        mListItems.add(item);
        // item 5分隔栏
        item = new CommonListItem();
        item.longLineText = "";
        mListItems.add(item);
        // item 6 帮助中心
        item = new CommonListItem();
        item.leftIconId = R.drawable.get_aid;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "帮助中心";
        item.onClickKey = aidCenter;
        mListItems.add(item);
        // item 7 关于我们
        item = new CommonListItem();
        item.leftIconId = R.drawable.about;
        item.rightIconId = R.drawable.arrow_next;
        item.title = "关于我们";
        item.onClickKey = about;
        mListItems.add(item);

        // item last 最后一个必须是这个
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
