package com.shanfu.tianxia.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.shanfu.tianxia.MainActivity;
import com.shanfu.tianxia.R;
import com.shanfu.tianxia.appconfig.Constants;
import com.shanfu.tianxia.base.BaseFragmentActivity;
import com.shanfu.tianxia.bean.RegeditBean;
import com.shanfu.tianxia.listener.DialogCallback;
import com.shanfu.tianxia.utils.AppUtils;
import com.shanfu.tianxia.utils.DateUtils;
import com.shanfu.tianxia.utils.MD5Utils;
import com.shanfu.tianxia.utils.SPUtils;
import com.shanfu.tianxia.utils.TUtils;
import com.shanfu.tianxia.utils.Urls;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 绑定银行卡
 */
public class BindingBankCardActivity extends BaseFragmentActivity implements View.OnClickListener {

    private RelativeLayout bind_card_top;
    private RelativeLayout content_head_back;
    private TextView content_head_title;
    @Bind(R.id.binding_bank_card_number)
    EditText binding_bank_card_number;
    @Bind(R.id.binding_bank_card_phone_number)
    EditText binding_bank_card_phone_number;
    @Bind(R.id.binding_card)
    Button binding_card;

    private String cardNum,phoneNum;
    private String t_status,p_status,b_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding_bank_card);
        ButterKnife.bind(this);
        t_status = SPUtils.getInstance().getString("t_status","");
        p_status = SPUtils.getInstance().getString("p_status","");
        b_status = SPUtils.getInstance().getString("b_status","");
        initView();
    }

    private void initView() {
        bind_card_top = (RelativeLayout) findViewById(R.id.bind_card_top);
        content_head_back = (RelativeLayout) bind_card_top.findViewById(R.id.content_head_back);
        content_head_title = (TextView) bind_card_top.findViewById(R.id.content_head_title);
        content_head_title.setText("添加银行卡");
        content_head_back.setOnClickListener(this);
        binding_card.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.content_head_back:
                finish();
                break;
            case R.id.binding_card:
                cardNum = binding_bank_card_number.getText().toString().trim();
                phoneNum = binding_bank_card_phone_number.getText().toString().trim();
                if(TextUtils.isEmpty(cardNum)){
                    TUtils.showShort(BindingBankCardActivity.this,"银行卡号不能为空");
                    return;
                }
                if(TextUtils.isEmpty(phoneNum)){
                    TUtils.showShort(BindingBankCardActivity.this,"手机号不能为空");
                    return;
                }
                if(phoneNum.length()!=11){
                    TUtils.showShort(BindingBankCardActivity.this, "请输入正确的手机号码");
                    return;
                }
                requestData(cardNum,phoneNum);
                break;


        }
    }
    private void requestData(String card,String num){
        try {
            String time = DateUtils.getLinuxTime();
            String token = MD5Utils.MD5(Constants.appKey + time);
            String version = AppUtils.getVerName(BindingBankCardActivity.this);
            String ptoken = SPUtils.getInstance().getString("ptoken", "");
            String uid = SPUtils.getInstance().getString("uid", "");
            HttpParams params = new HttpParams();
            params.put("time", time);
            params.put("token", token);
            params.put("bankCardNum", card);
            params.put("prePhone", num);
            params.put("version", version);
            params.put("ptoken", ptoken);
            params.put("uid", uid);

            OkGo.post(Urls.bindBankCard)
                    .tag(this)
                    .params(params)
                    .execute(new DialogCallback<RegeditBean>(this) {
                        @Override
                        public void onSuccess(RegeditBean rsultBean, Call call, Response response) {
                            decodeResult(rsultBean);
                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            TUtils.showShort(BindingBankCardActivity.this, "数据获取失败，请检查网络后重试");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decodeResult(RegeditBean rsultBean){
        String err_code = rsultBean.getErr_code();
        String msg = rsultBean.getErr_msg();
        if("200".equals(err_code)){
            TUtils.showShort(BindingBankCardActivity.this, "绑定成功");
            SPUtils.getInstance().putString("ptoken", rsultBean.getData().getPtoken());
            SPUtils.getInstance().putString("b_status", "1");
            SPUtils.getInstance().putString("bankcard", cardNum);
            Intent intent = new Intent(BindingBankCardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("comefrom","mine");
            startActivity(intent);
            finish();
        }else if("103".equals(err_code)){
            Intent intent = new Intent(BindingBankCardActivity.this,LoginActivity.class);
            startActivity(intent);
        }else{
            TUtils.showShort(BindingBankCardActivity.this, msg);
        }


    }
}
