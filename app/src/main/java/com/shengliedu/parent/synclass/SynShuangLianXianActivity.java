package com.shengliedu.parent.synclass;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shengliedu.parent.BaseActivity;
import com.shengliedu.parent.R;
import com.shengliedu.parent.adapter.SynLianXianAdapter;
import com.shengliedu.parent.app.App;
import com.shengliedu.parent.bean.AfterClassLianXian;
import com.shengliedu.parent.pdf.ShowPdfFromUrlActivity;
import com.shengliedu.parent.util.Config1;
import com.shengliedu.parent.util.HtmlImage;
import com.shengliedu.parent.util.NumberToCode;
import com.shengliedu.parent.util.ResultCallback;
import com.shengliedu.parent.view.NoScrollListview;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class SynShuangLianXianActivity extends BaseActivity {
	private String date;
	private String content_host;
	private String questionTypeName;
	private String questionContentType;
	private int studentId;
	private int coursewareContentId;
	private int hourId;
	
	public TextView select_title, pandun, zhengquelv, syn_question_head;
	public Button select_btn;
	private String ques = "";
	private String rques = "";
	private Map<Integer, Boolean> map1 = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
	private NoScrollListview listView;
	private NoScrollListview listView1;
	private AfterClassLianXian data;
	private LinearLayout shuanglianxLin;
	private TextView select_pdf;
	Handler handlerReq=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what==1){
				JSONObject result=JSON.parseObject((String) msg.obj);
				List<AfterClassLianXian> dataListTemp = JSON.parseArray(result.getString("data"), AfterClassLianXian.class);
				if (dataListTemp != null) {
					data=dataListTemp.get(0);
					Spanned spanned = Html.fromHtml(HtmlImage.deleteSrc(data.question.question));
					select_title.setText(spanned);
					String name = data.question.question;
					List<String> imgList = HtmlImage.getImgSrc(name);
					if (imgList.size() > 0) {
						for (int j = 0; j < imgList.size(); j++) {
							final String url = imgList.get(j);
							ImageView imageView = new ImageView(
									SynShuangLianXianActivity.this);
							imageView.setScaleType(ScaleType.FIT_CENTER);
							imageView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
							ImageLoader.getInstance().displayImage(url, imageView);
							imageView.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated
									// method stub
									HtmlImage htmlImage=new HtmlImage();
									htmlImage.showDialog(SynShuangLianXianActivity.this,url);
								}
							});
							shuanglianxLin.addView(imageView);
						}
					}
					if (data.studentAnswer != null
							&& data.studentAnswer.size() > 0) {
						ques = data.studentAnswer.get(0).title;
					}
					rques = data.correct.get(0).title;
					Log.e("ques", "haha" + ques);
					Log.e("rques", rques);
					String per = data.percentage;
					Log.e("per", per);
					if (ques != null && ques.length() == 2) {
						map.put(NumberToCode.codeToNumber(ques.substring(0, 1)),
								true);
						map1.put(NumberToCode.codeToNumber(ques.substring(1, 2)),
								true);
					}
					if ("1".equals(questionContentType)) {
						select_pdf.setVisibility(View.VISIBLE);
						select_pdf.setText("查看PDF题目");
						select_pdf.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								Intent intent=new Intent(SynShuangLianXianActivity.this,ShowPdfFromUrlActivity.class);
								intent.putExtra("url", Config1.IP + data.question.contentLink);
								startActivity(intent);
							}
						});
					} else {
						select_pdf.setVisibility(View.GONE);
					}
					SynLianXianAdapter adapter = new SynLianXianAdapter(
							SynShuangLianXianActivity.this, data.answer
							.answersF.get(0).items, map);
					listView.setAdapter(adapter);
					SynLianXianAdapter adapter1 = new SynLianXianAdapter(
							SynShuangLianXianActivity.this, data.answer
							.answersF.get(1).items, map1);
					listView1.setAdapter(adapter1);
					String rqueStr = "";
					if (data.correct != null & data.correct.size() != 0) {
						rques = data.correct.get(0).title;
						for (int i = 0; i < data.correct.size(); i++) {
							rqueStr = rqueStr + data.correct.get(i).title
									+ " ";
						}
					}
					int i = 0;
					String text = "";
					if (data.studentAnswer != null
							&& data.studentAnswer.size() > 0) {
						for (int j = 0; j < data.studentAnswer.size(); j++) {
							if (rqueStr.contains(data.studentAnswer.get(i)
									.title)) {
								i++;
							}
							text = text + data.studentAnswer.get(j).title
									+ " ";
						}
						if (!"".equals(rqueStr)) {
							if (data.correct.size() == i) {
								zhengquelv.setText("您孩子的答案是" + text + ",正确答案是"
										+ rqueStr + ",回答正确！" + "全班回答正确率 " + per
										+ "%");

							} else {
								zhengquelv.setText("您孩子的答案是" + text + ",正确答案是"
										+ rqueStr + ",回答错误！" + "全班回答正确率 " + per
										+ "%");
							}

						} else {
							zhengquelv.setText("您孩子未回答 正确答案是" + rqueStr
									+ " 全班回答正确率 " + per + "%");
						}
					}

				}
			}else if (msg.what==2){

			}
		}
	};
	@Override
	public void initViews() {
		// TODO Auto-generated method stub
		coursewareContentId = (Integer) getIntent().getExtras().get(
				"coursewareContentId");
		studentId = App.childInfo.id;
		date = (String) getIntent().getExtras().get("date");
		content_host = (String) getIntent().getExtras().get("content_host");
		hourId = (Integer) getIntent().getExtras().get("hours");
		questionTypeName = (String) getIntent().getExtras().get(
				"questionTypeName");
		questionContentType = (String) getIntent().getExtras().get(
				"questionContentType");
		setBack();
		showTitle(questionTypeName);
		shuanglianxLin = (LinearLayout) findViewById(R.id.shuanglianxLin);
		select_title = (TextView) findViewById(R.id.syn_question_title);
		select_pdf = (TextView) findViewById(R.id.select_pdf);
		pandun = (TextView) findViewById(R.id.syn_question_pandun);
		listView = (NoScrollListview) findViewById(R.id.syn_question_lv);
		listView1 = (NoScrollListview) findViewById(R.id.syn_question_lv1);
		select_btn = (Button) findViewById(R.id.jiexi);
		zhengquelv = (TextView) findViewById(R.id.zhengquelv);
		select_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent iteIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("explanation", data.explanation);
				iteIntent.putExtras(bundle);
				iteIntent.setClass(SynShuangLianXianActivity.this,
						SynSeletQuestionAnalyticalActivity.class);
				startActivity(iteIntent);
			}
		});
	}

	@Override
	public void getDatas() {
		// TODO Auto-generated method stub
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("studentId", studentId);
		map2.put("hourId", hourId);
		map2.put("date", date);
		map2.put("coursewareContentId", coursewareContentId);
		doGet(content_host + Config1.getInstance().SYNCLASSSINGLE(), map2,
				new ResultCallback() {
					@Override
					public void onResponse(Call call, Response response, String json) {
						Message message=Message.obtain();
						message.what=1;
						message.obj=json;
						handlerReq.sendMessage(message);
					}

					@Override
					public void onFailure(Call call, IOException exception) {
						handlerReq.sendEmptyMessage(2);
					}
				});
	}

	@Override
	public int setLayout() {
		// TODO Auto-generated method stub
		return R.layout.activity_syn_shuanglianxian;
	}

}
