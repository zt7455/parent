package com.shengliedu.parent.new_synclass;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shengliedu.parent.BaseActivity;
import com.shengliedu.parent.R;
import com.shengliedu.parent.bean.BeanYunIp;
import com.shengliedu.parent.new_homework.bean.NewHomeworkAnswer;
import com.shengliedu.parent.new_homework.bean.NewHomeworkDetail;
import com.shengliedu.parent.new_synclass.bean.BeanSynClass;
import com.shengliedu.parent.pdf.ShowPdfFromUrlActivity;
import com.shengliedu.parent.util.Config1;
import com.shengliedu.parent.util.HtmlImage;
import com.shengliedu.parent.util.ResultCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class NewSynClassDetailActivity extends BaseActivity {
	private BeanSynClass currentHome;
	private BeanYunIp yunIp;
	private String currentIp;
	private String asnwer="";

	private LinearLayout mainLin,questionLin;
	private TextView select_main,select_question,select_pdf,select_answer,select_wrong;
	private NewHomeworkDetail newHomeworkDetail;
	@Override
	public void initViews() {
		// TODO Auto-generated method stub
		currentHome= (BeanSynClass) getIntent().getExtras().getSerializable("SynClass");
		setBack();
		mainLin=getView(R.id.mainLin);
		questionLin=getView(R.id.questionLin);
		select_main=getView(R.id.select_main);
		select_question=getView(R.id.select_question);
		select_pdf=getView(R.id.select_pdf);
		select_answer=getView(R.id.select_answer);
		select_wrong=getView(R.id.select_wrong);
	}
	Handler handlerReq=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what==1){
				List<BeanYunIp> beanYunIps=JSON.parseArray((String) msg.obj,BeanYunIp.class);
				if (beanYunIps!=null && beanYunIps.size()>0){
					yunIp = beanYunIps.get(0);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("isSingle", 1);
					currentIp="http://" + yunIp.ip+ "/";
		        doGet(currentIp+ "ques/"+currentHome.content_id+"?", map,
				new ResultCallback() {
					@Override
					public void onResponse(Call call, Response response, String json) {
						Message message=Message.obtain();
						message.what=3;
						message.obj=json;
						handlerReq.sendMessage(message);
					}

					@Override
					public void onFailure(Call call, IOException exception) {
						handlerReq.sendEmptyMessage(4);
					}
				});
				}
			}else if (msg.what==2){

			}else if (msg.what==3){
				if (currentHome.content_type==2) {
				List<NewHomeworkDetail> newHomeworkDetails=JSON.parseArray((String) msg.obj,NewHomeworkDetail.class);
				if (newHomeworkDetails!=null && newHomeworkDetails.size()>0){
					newHomeworkDetail=newHomeworkDetails.get(0);
					final NewHomeworkDetail question= JSON.parseObject(newHomeworkDetail.question,NewHomeworkDetail.class);
					if (question!=null && !TextUtils.isEmpty(question.main)){
						mainLin.setVisibility(View.VISIBLE);
							deal_text(question.main,mainLin,select_main);
					}else {
						mainLin.setVisibility(View.GONE);
					}
					if (question!=null && !TextUtils.isEmpty(question.question)){
						questionLin.setVisibility(View.VISIBLE);
						deal_text(question.question,questionLin,select_question);
						if (!TextUtils.isEmpty(question.contentLink) && question.contentLink.toLowerCase().endsWith(".pdf")){
							select_pdf.setVisibility(View.VISIBLE);
							select_pdf.setText("查看PDF题目");
							select_pdf.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									Intent intent=new Intent(NewSynClassDetailActivity.this,ShowPdfFromUrlActivity.class);
									intent.putExtra("url", currentIp + question.contentLink);
									startActivity(intent);
								}
							});
						}else {
							select_pdf.setVisibility(View.GONE);
						}
					}else {
						questionLin.setVisibility(View.GONE);
					}
						NewHomeworkDetail answer = JSON.parseObject(newHomeworkDetail.answer, NewHomeworkDetail.class);
						if (answer != null && answer.answers != null && answer.answers.size() > 0) {
							select_answer.setVisibility(View.VISIBLE);
							String answers = "";
							for (int i = 0; i < answer.answers.size(); i++) {
								answers += answer.answers.get(i).get(0).title + "、" + answer.answers.get(i).get(0).content + "\n";
							}
							select_answer.setText(answers);

						} else {
							select_answer.setVisibility(View.GONE);
						}

						if (currentHome != null && !TextUtils.isEmpty(currentHome.answer)) {
							List<NewHomeworkAnswer> newHomeworkAnswers = JSON.parseArray(currentHome.answer, NewHomeworkAnswer.class);
							int wrong=1;
							if (newHomeworkAnswers != null && newHomeworkAnswers.size() > 0) {
								if (newHomeworkDetail != null && !TextUtils.isEmpty(newHomeworkDetail.correct)) {
									List<NewHomeworkDetail> corrects = JSON.parseArray(newHomeworkDetail.correct, NewHomeworkDetail.class);
									if (corrects != null && corrects.size() > 0) {
										String a = corrects.get(0).title;
										if (asnwer.equals(a)) {
											wrong = 0;
										}
									}
								}
								asnwer = newHomeworkAnswers.get(0).data;
								String text="您的孩子答案是: "+asnwer+" 回答"+((wrong==0)?"正确":"错误");
								select_wrong.setText(text);
							}
						}
				      }else {
								toast("课堂信息获取失败");
							}
					}
			}else if (msg.what==4){

			}
		}
	};
	@Override
	public void getDatas() {
		// TODO Auto-generated method stub
		if (currentHome!=null){
			if (currentHome.from==1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("schoolinfo_id", currentHome.school);
				doGet(Config1.getInstance().GETSCHOOLINFO(), map,
						new ResultCallback() {
							@Override
							public void onResponse(Call call, Response response, String json) {
								Message message = Message.obtain();
								message.what = 1;
								message.obj = json;
								handlerReq.sendMessage(message);
							}

							@Override
							public void onFailure(Call call, IOException exception) {
								handlerReq.sendEmptyMessage(2);
							}
						});
			}else if (currentHome.from==2) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("isSingle", 1);
				currentIp=Config1.getInstance().IP;
				doGet(currentIp+"ques/"+currentHome.content_id+"?", map,
						new ResultCallback() {
							@Override
							public void onResponse(Call call, Response response, String json) {
								Message message=Message.obtain();
								message.what=3;
								message.obj=json;
								handlerReq.sendMessage(message);
							}

							@Override
							public void onFailure(Call call, IOException exception) {handlerReq.sendEmptyMessage(4);

							}
						});
			}
		}
	}

	@Override
	public int setLayout() {
		// TODO Auto-generated method stub
		return R.layout.activity_new_synclass_detail;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void deal_text(String text,LinearLayout linearLayout,TextView textView){
		textView.setText(Html.fromHtml(HtmlImage
				.deleteSrc(text)));
		List<String> imgList = HtmlImage.getImgSrc(text);
		ImageLoader utils = ImageLoader.getInstance();
		if (imgList.size() > 0) {
			for (int j = 0; j < imgList.size(); j++) {
				final String url = imgList.get(j);
				ImageView imageView = new ImageView(this);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setLayoutParams(new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				utils.displayImage(url, imageView);
				imageView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated
						// method stub
						HtmlImage htmlImage = new HtmlImage();
						htmlImage.showDialog(NewSynClassDetailActivity.this, url);
					}
				});
				linearLayout.addView(imageView);
			}
		}
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


}
