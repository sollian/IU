
package com.aiyou.bbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.FaceViewListener.MyGridViewListener;
import com.aiyou.bbs.FaceViewListener.MyOnPageChangeListener;
import com.aiyou.bbs.FileSelect.DirectoryChooserView;
import com.aiyou.bbs.FileSelect.DirectoryChooserView.ChosenDirectoryListener;
import com.aiyou.bbs.adapter.FaceGridViewAdapter;
import com.aiyou.bbs.adapter.ViewPagerAdapter;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Attachment;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Mail;
import com.aiyou.bbs.bean.Mailbox.MailboxType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.image.ImageFactory;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.view.ControlScrollViewPager;
import com.aiyou.view.CustomDialog;
import com.aiyou.view.ScrollTextView;

import external.ColorPicker.ColorPickerDialog;
import external.ColorPicker.ColorPickerPreference;
import external.ColorPicker.ColorPickerDialog.OnColorChangedListener;
import external.GifImageViewEx.net.frakbot.imageviewex.Converters;
import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewEx;
import external.OtherView.Win8ProgressBar;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 发表文章、回复、写信
 * 
 * @author sollian
 */
public class BBSWriteActivity extends BaseActivity implements OnClickListener,
        ChosenDirectoryListener, OnColorChangedListener,
        OnItemLongClickListener, OnTouchListener {
    public static final String NEW_MAIL = "newMail";
    public static final String MAIL_TO = "mailTo";
    public static final String WRITE_ARTICLE = "write_article";
    public static final String REPLY_ARTICLE = "reply_article";
    public static final String EDIT_ARTICLE = "edit_article";
    /**
     * 5项功能
     */
    private final static int ARTICLE = 0;
    private final static int BOARD = 1;
    private final static int MAIL = 2;
    private final static int EDIT = 3;
    private final static int NEWMAIL = 4;

    private static final int RESULT_LOAD_IMAGE = 0x1110;
    private static final int RESULT_TAKE_PHOTO = 0x1120;
    private static final int MSG_ARTICLE_SEND = 0;
    private static final int MSG_ERROR = -1;
    private static final String KEY_DATA = "data";

    private static final float BMP_WIDTH = 72f;
    private static final float BMP_HEIGHT = 72f;

    private SwitchManager mSwitchMgr;
    private BBSManager mBBSMgr;
    private AiYouManager mIUMgr;

    // 用于为拍摄的照片命名
    private String mTimeStamp;
    /**
     * 取值为ARTICLE|BOARD|MAIL|EDIT|NEWMAIL
     */
    private int mMode;
    // 回复的Article
    private Article mArticle;
    // 发表新话题所在的版面
    private Board mBoard;
    // 回信
    private Mail mMail;
    /**
     * viewpager相关
     */
    private ArrayList<View> mTabList = new ArrayList<View>(); // Tab页面列表
    /**
     * 附件相关
     */
    // 文件选择对话框
    private DirectoryChooserView mDirChooserView;
    // 存储附件的列表
    private ArrayList<String> mAttachList = new ArrayList<String>();
    /**
     * 拖动附件显示的图片
     */
    private ImageView mDragView;
    private Bitmap mDragBmp;

    /**
     * 拾色器对话框
     */
    private ColorPickerDialog mColorDialog;
    /**
     * 动态表情相关
     */
    private int vpLocation[] = new int[2];
    private ArrayList<HashMap<String, String>> mDynamicFaceList = new ArrayList<HashMap<String, String>>();
    /**
     * 控件
     */
    // 标题
    private ScrollTextView mTitleSTV;
    // 主题
    private EditText mSubjectET;
    // 收信人，仅在撰写新邮件时可见
    private EditText mToET;
    // 内容
    private EditText mContentET;
    // 表情相关
    private LinearLayout mFaceLLayout;
    private ControlScrollViewPager mViewPager;
    private ImageView mCursorIV;
    private TextView mClassicTV, mOnionTV, mTuzkiTV,
            mYociTV;
    private GridView mClassicGV, mOnionGV, mTuzkiGV,
            mYociGV;
    /*
     * 查看动态表情的view
     */
    private LinearLayout mDynamicFaceLLayout;
    private ImageViewEx mDynamicFaceIVE;
    // help
    private ImageView mHelpIV;
    // 转载附件的布局
    private LinearLayout mAttachLLayout;
    // 选项
    private ImageView mFaceIV, mPhotoIV, mCameraIV,
            mAttachIV, mFontIV;
    // 文字选项布局
    private LinearLayout mFontLLayout;
    // 文字大小选择布局
    private LinearLayout mFontSizeLLayout;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_ARTICLE_SEND == msg.what) {
                String info = null;
                if (ARTICLE == mMode) {
                    info = "回复成功";
                } else if (BOARD == mMode) {
                    info = "发表成功";
                } else if (EDIT == mMode) {
                    info = "更新成功";
                } else if (MAIL == mMode) {
                    info = "回复成功";
                } else if (NEWMAIL == mMode) {
                    info = "发送成功";
                }
                if (!TextUtils.isEmpty(info)) {
                    Toast.makeText(getBaseContext(), info, Toast.LENGTH_SHORT).show();
                }
                scrollToFinishActivity();
            } else if (MSG_ERROR == msg.what) {
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                if (TextUtils.isEmpty(strError)) {
                    strError = NetWorkManager.MSG_NONET;
                }
                // 连接服务器失败
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
            }
            showProgress(false);
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_bbs_write);

        init();

        Intent intent = getIntent();
        mBoard = (Board) intent.getSerializableExtra(WRITE_ARTICLE);
        if (null != mBoard) {
            // 发表
            mMode = BOARD;
            mTitleSTV.setText("新话题");
            if (!mBoard.allow_attachment) {
                mPhotoIV.setVisibility(View.INVISIBLE);
                mCameraIV.setVisibility(View.INVISIBLE);
                mAttachIV.setVisibility(View.INVISIBLE);
            }
            return;
        }
        mArticle = (Article) intent.getSerializableExtra(EDIT_ARTICLE);
        if (mArticle != null) {
            // 编辑
            mMode = EDIT;
            mTitleSTV.setText("编辑");
            mSubjectET.setText(mArticle.title);
            mContentET.setText(mArticle.content);
            if (!mBBSMgr.isAllowAttachment(mArticle.board_name)) {
                mPhotoIV.setVisibility(View.INVISIBLE);
                mCameraIV.setVisibility(View.INVISIBLE);
                mAttachIV.setVisibility(View.INVISIBLE);
            }
            return;
        }
        mArticle = (Article) intent.getSerializableExtra(REPLY_ARTICLE);
        if (null != mArticle) {
            // 回复
            mMode = ARTICLE;
            mTitleSTV.setText("新回复");
            mSubjectET.setText("Re:" + mArticle.title);
            if (!mBBSMgr.isAllowAttachment(mArticle.board_name)) {
                mPhotoIV.setVisibility(View.INVISIBLE);
                mCameraIV.setVisibility(View.INVISIBLE);
                mAttachIV.setVisibility(View.INVISIBLE);
            }
            return;
        }
        mMail = (Mail) intent.getSerializableExtra(NEW_MAIL);
        if (null != mMail) {
            // 回信
            mMode = MAIL;
            mTitleSTV.setText("回信:" + mMail.user.id);
            mSubjectET.setText("Re:" + mMail.title);
            mPhotoIV.setVisibility(View.INVISIBLE);
            mCameraIV.setVisibility(View.INVISIBLE);
            mAttachIV.setVisibility(View.INVISIBLE);
            return;
        }
        String strTo = intent.getStringExtra(MAIL_TO);
        // 新邮件
        mMode = NEWMAIL;
        mTitleSTV.setText("新邮件");
        mToET.setVisibility(View.VISIBLE);
        mPhotoIV.setVisibility(View.INVISIBLE);
        mCameraIV.setVisibility(View.INVISIBLE);
        mAttachIV.setVisibility(View.INVISIBLE);
        if (null != strTo) {
            mToET.setText(strTo);
        }
    }

    @SuppressLint("InflateParams")
    private void init() {
        mBBSMgr = BBSManager.getInstance(getBaseContext());
        mIUMgr = AiYouManager.getInstance(getBaseContext());
        /**
         * 拾色器
         */
        mColorDialog = new ColorPickerDialog(this, Color.BLACK);
        mColorDialog.setOnColorChangedListener(this);
        /**
         * 动态表情
         */
        mDynamicFaceLLayout = (LinearLayout) findViewById(R.id.activity_bbswrite_ll_ive);
        mDynamicFaceIVE = (ImageViewEx) findViewById(R.id.activity_bbswrite_ive);
        /**
         * help
         */
        mHelpIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_help);

        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_bbswrite_stv_title);

        mSubjectET = (EditText) findViewById(R.id.activity_bbswrite_et_subject);
        mToET = (EditText) findViewById(R.id.activity_bbswrite_et_to);
        mContentET = (EditText) findViewById(R.id.activity_bbswrite_et_content);

        mFaceLLayout = (LinearLayout) findViewById(R.id.activity_bbswrite_ll_face);
        mViewPager = (ControlScrollViewPager) findViewById(R.id.activity_bbswrite_vp);
        mCursorIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_cursor);
        mClassicTV = (TextView) findViewById(R.id.activity_bbswrite_tv_classic);
        mOnionTV = (TextView) findViewById(R.id.activity_bbswrite_tv_onion);
        mTuzkiTV = (TextView) findViewById(R.id.activity_bbswrite_tv_tuzki);
        mYociTV = (TextView) findViewById(R.id.activity_bbswrite_tv_yoci);

        mAttachLLayout = (LinearLayout) findViewById(R.id.activity_bbswrite_ll_attach);

        mFaceIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_face);
        mPhotoIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_photo);
        mCameraIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_camera);
        mAttachIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_attachment);
        mFontIV = (ImageView) findViewById(R.id.activity_bbswrite_iv_font);

        mFontLLayout = (LinearLayout) findViewById(R.id.activity_bbswrite_ll_font);
        mFontSizeLLayout = (LinearLayout) findViewById(R.id.activity_bbswrite_ll_font_size);

        mDragView = (ImageView) findViewById(R.id.activity_bbswrite_iv_drag);

        mDirChooserView = (DirectoryChooserView) findViewById(R.id.activity_bbswrite_dsv);
        mDirChooserView.setChosenDirectoryListener(this);

        mFaceIV.setOnClickListener(this);
        mPhotoIV.setOnClickListener(this);
        mCameraIV.setOnClickListener(this);
        mAttachIV.setOnClickListener(this);
        mFontIV.setOnClickListener(this);

        // 动画图片宽度
        int bmpW = BitmapFactory.decodeResource(getResources(),
                R.drawable.face_cursor).getWidth();// 获取图片宽度
        // 动画图片偏移量
        int offset = ((AiYouManager.getScreenWidth() - mIUMgr.dip2px(20)) / 4 - bmpW) / 2;// 计算偏移量

        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        mCursorIV.setImageMatrix(matrix);// 设置动画初始位置
        // 页卡
        mClassicTV.setOnClickListener(new FaceTypeListener(0));
        mOnionTV.setOnClickListener(new FaceTypeListener(1));
        mTuzkiTV.setOnClickListener(new FaceTypeListener(2));
        mYociTV.setOnClickListener(new FaceTypeListener(3));
        // viewpager
        LayoutInflater mInflater = getLayoutInflater();
        mClassicGV = (GridView) mInflater.inflate(R.layout.face_classic, null);
        mOnionGV = (GridView) mInflater.inflate(R.layout.face_onion, null);
        mTuzkiGV = (GridView) mInflater.inflate(R.layout.face_tuzki, null);
        mYociGV = (GridView) mInflater.inflate(R.layout.face_yoci, null);
        mClassicGV.setAdapter(new FaceGridViewAdapter(getBaseContext(), 0));
        mOnionGV.setAdapter(new FaceGridViewAdapter(getBaseContext(), 1));
        mTuzkiGV.setAdapter(new FaceGridViewAdapter(getBaseContext(), 2));
        mYociGV.setAdapter(new FaceGridViewAdapter(getBaseContext(), 3));
        mClassicGV.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 0, mContentET));
        mOnionGV.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 1, mContentET));
        mTuzkiGV.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 2, mContentET));
        mYociGV.setOnItemClickListener(new MyGridViewListener(getBaseContext(),
                3, mContentET));
        if (Build.VERSION.SDK_INT >= 11) {
            mClassicGV.setOnItemLongClickListener(this);
            mOnionGV.setOnItemLongClickListener(this);
            mTuzkiGV.setOnItemLongClickListener(this);
            mYociGV.setOnItemLongClickListener(this);
            mClassicGV.setOnTouchListener(this);
            mOnionGV.setOnTouchListener(this);
            mTuzkiGV.setOnTouchListener(this);
            mYociGV.setOnTouchListener(this);
        }
        mTabList.add(mClassicGV);
        mTabList.add(mOnionGV);
        mTabList.add(mTuzkiGV);
        mTabList.add(mYociGV);
        mViewPager.setAdapter(new ViewPagerAdapter(mTabList));
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener(mCursorIV,
                offset, bmpW, 0));
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
    }

    private class FaceTypeListener implements OnClickListener {
        private int index = 0;

        public FaceTypeListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(index);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mFaceIV) {
            // 打开|关闭表情栏
            if (View.VISIBLE == mFaceLLayout.getVisibility()) {
                mFaceLLayout.setVisibility(View.GONE);
            } else {
                mFaceLLayout.setVisibility(View.VISIBLE);
                if (mSwitchMgr.needShowFaceHelp()) {
                    mHelpIV.setVisibility(View.VISIBLE);
                    mHelpIV.bringToFront();
                    mSwitchMgr.disableShowFaceHelp();
                }
            }
        } else if (v == mPhotoIV) {
            if (mAttachList.size() == 0) {
                Toast.makeText(getBaseContext(), "向下拖动附件可删除",
                        Toast.LENGTH_SHORT).show();
            }
            // 选择照片
            if (mAttachList.size() >= 20) {
                Toast.makeText(getBaseContext(), "附件数量已达上限", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
            overridePendingTransition(android.R.anim.fade_in, 0);
        } else if (v == mCameraIV) {
            if (mAttachList.size() == 0) {
                Toast.makeText(getBaseContext(), "向下拖动附件可删除",
                        Toast.LENGTH_SHORT).show();
            }
            // 相机拍照
            if (mAttachList.size() >= 20) {
                Toast.makeText(getBaseContext(), "附件数量已达上限", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            long time = System.currentTimeMillis() / 1000;
            mTimeStamp = time + "";
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + FileManager.DIR_CAMERA;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
            i.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(path, mTimeStamp + FileManager.BMP_SUFFIX)));
            startActivityForResult(i, RESULT_TAKE_PHOTO);
            overridePendingTransition(android.R.anim.fade_in, 0);
        } else if (v == mAttachIV) {
            if (mAttachList.size() == 0) {
                Toast.makeText(getBaseContext(), "向下拖动附件可删除",
                        Toast.LENGTH_SHORT).show();
            }
            // 选择附件
            if (mAttachList.size() >= 20) {
                Toast.makeText(getBaseContext(), "附件数量已达上限", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            mDirChooserView.chooseDirectory();
        } else if (v == mFontIV) {
            // 打开|关闭字体设置面板
            if (View.VISIBLE == mFontLLayout.getVisibility()) {
                mFontLLayout.setVisibility(View.GONE);
            } else {
                mFontLLayout.setVisibility(View.VISIBLE);
            }
            mFontSizeLLayout.setVisibility(View.GONE);
        } else if (R.id.activity_bbswrite_iv_help == v.getId()) {
            mHelpIV.setVisibility(View.GONE);
        }
    }

    public void onFontClick(View view) {
        int nId = view.getId();
        if (R.id.activity_bbswrite_iv_font_bold == nId) {
            // 粗体
            Editable edit = mContentET.getText();
            int start = mContentET.getSelectionStart();
            int end = mContentET.getSelectionEnd();
            edit.insert(end, "[/b]");
            edit.insert(start, "[b]");
            mContentET.setSelection(start + 3);
        } else if (R.id.activity_bbswrite_iv_font_italic == nId) {
            // 斜体
            Editable edit = mContentET.getText();
            int start = mContentET.getSelectionStart();
            int end = mContentET.getSelectionEnd();
            edit.insert(end, "[/i]");
            edit.insert(start, "[i]");
            mContentET.setSelection(start + 3);
        } else if (R.id.activity_bbswrite_iv_font_underline == nId) {
            // 下划线
            Editable edit = mContentET.getText();
            int start = mContentET.getSelectionStart();
            int end = mContentET.getSelectionEnd();
            edit.insert(end, "[/u]");
            edit.insert(start, "[u]");
            mContentET.setSelection(start + 3);
        } else if (R.id.activity_bbswrite_iv_font_size == nId) {
            // 打开|关闭字体大小面板
            if (View.VISIBLE == mFontSizeLLayout.getVisibility()) {
                mFontSizeLLayout.setVisibility(View.GONE);
            } else {
                mFontSizeLLayout.setVisibility(View.VISIBLE);
            }
        } else if (R.id.activity_bbswrite_iv_font_color == nId) {
            // 打开字体颜色对话框
            mColorDialog.show();
            return;
        }
        // 打开输入法
        AiYouManager.viewInputMethod(BBSWriteActivity.this, true, mContentET);
    }

    public void onFontSizeClick(View view) {
        int nId = view.getId();
        int size = 1;
        if (R.id.activity_bbswrite_tv_font_size_1 == nId) {
            size = 1;
        } else if (R.id.activity_bbswrite_tv_font_size_2 == nId) {
            size = 2;
        } else if (R.id.activity_bbswrite_tv_font_size_3 == nId) {
            size = 3;
        } else if (R.id.activity_bbswrite_tv_font_size_4 == nId) {
            size = 4;
        } else if (R.id.activity_bbswrite_tv_font_size_5 == nId) {
            size = 5;
        } else if (R.id.activity_bbswrite_tv_font_size_6 == nId) {
            size = 6;
        } else if (R.id.activity_bbswrite_tv_font_size_7 == nId) {
            size = 7;
        } else if (R.id.activity_bbswrite_tv_font_size_8 == nId) {
            size = 8;
        } else if (R.id.activity_bbswrite_tv_font_size_9 == nId) {
            size = 9;
        }
        Editable edit = mContentET.getText();
        int start = mContentET.getSelectionStart();
        int end = mContentET.getSelectionEnd();
        edit.insert(end, "[/size]");
        edit.insert(start, "[size=" + size + "]");
        mContentET.setSelection(start + 8);
        // 打开输入法
        AiYouManager.viewInputMethod(BBSWriteActivity.this, true, mContentET);
    }

    @Override
    public void onColorChanged(int color) {
        String strColor = ColorPickerPreference.convertToRGB(color);
        Editable edit = mContentET.getText();
        int start = mContentET.getSelectionStart();
        int end = mContentET.getSelectionEnd();
        edit.insert(end, "[/color]");
        edit.insert(start, "[color=" + strColor + "]");
        mContentET.setSelection(start + 15);
    }

    /**
     * 发送
     * 
     * @param view
     */
    public void onSend(View view) {
        String subject = mSubjectET.getText().toString().trim();
        String content = mContentET.getText().toString().trim();
        if ("".equals(subject) || "".equals(content)) {
            Toast.makeText(getBaseContext(), "主题或内容为空", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        // 关闭输入法
        AiYouManager.viewInputMethod(BBSWriteActivity.this, false, mContentET);
        AiYouManager.viewInputMethod(BBSWriteActivity.this, false, mToET);
        AiYouManager.viewInputMethod(BBSWriteActivity.this, false, mSubjectET);
        // 设置进度条
        showProgress(true);
        String tail = mBBSMgr.getAppTail();
        if (NEWMAIL == mMode) {
            String strTo = mToET.getText().toString().trim();
            if ("".equals(strTo)) {
                Toast.makeText(getBaseContext(), "收信人为空", Toast.LENGTH_SHORT)
                        .show();
                mToET.requestFocus();
                return;
            }
            // 添加应用小尾巴
            if (null != tail) {
                String str = content.trim();
                if (!str.endsWith(tail)) {
                    content += "\n\n" + tail;
                }
            }
            threadSendArticle(subject, content, strTo);
            return;
        }
        if (ARTICLE == mMode || MAIL == mMode) {
            // 回复
            String strContent = "";
            if (ARTICLE == mMode) {
                String[] arr = JsonHelper.toHtml(mArticle, true);
                String strReply = arr[1];
                strContent = mArticle.content.replace(strReply, "").trim();
            } else if (MAIL == mMode) {
                String[] arr = JsonHelper.toHtml(mMail, false);
                String strReply = arr[1];
                strContent = mMail.content.replace(strReply, "").trim();
            }
            // 去除多余的尾巴
            while (strContent.endsWith("-") || strContent.endsWith("\n")) {
                strContent = strContent.substring(0, strContent.length() - 1);
            }
            if (strContent.length() > 500) {
                strContent = strContent.substring(0, 500);
                strContent += "\n...................";
            }
            String[] array = strContent.split("\n");
            strContent = "";
            for (int i = 0; i < array.length; i++) {
                array[i] = AiYouManager.getTxtWithoutNTSRElement(array[i], "");
                array[i] = array[i].trim();
                if (!array[i].equals("")) {
                    if (i < array.length - 1) {
                        strContent += array[i] + "\n: ";
                    } else {
                        strContent += array[i];
                    }
                }
            }
            if (ARTICLE == mMode) {
                content = content + "\n【 在 " + mArticle.user.id
                        + " 的大作中提到: 】\n: " + strContent;
            } else if (MAIL == mMode) {
                content = content + "\n【 在 " + mMail.user.id + " 的大作中提到: 】\n: "
                        + strContent;
            }
        }
        // 添加应用小尾巴
        if (null != tail) {
            if (mMode == EDIT) {
                // 去除多余的尾巴
                while (content.endsWith("-") || content.endsWith("\n")) {
                    content = content.substring(0, content.length() - 1);
                }
            }
            String str = content.trim();
            if (!str.endsWith(tail)) {
                content += "\n\n" + tail;
            }
        }
        threadSendArticle(subject, content, null);
    }

    private void threadSendArticle(final String subject, final String content,
            final String strTo) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = null;
                if (ARTICLE == mMode) {
                    // 新回复
                    strJson = Article.sendArticle(BBSWriteActivity.this, mArticle.board_name,
                            subject, content, mArticle.id + "");
                } else if (BOARD == mMode) {
                    // 新话题
                    strJson = Article.sendArticle(BBSWriteActivity.this, mBoard.name, subject,
                            content, null);
                } else if (EDIT == mMode) {
                    // 编辑
                    strJson = Article.updateArticle(BBSWriteActivity.this, mArticle.board_name,
                            mArticle.id, subject, content);
                } else if (NEWMAIL == mMode) {
                    // 新邮件
                    strJson = Mail.sendMail(BBSWriteActivity.this, subject, content, strTo);
                } else if (MAIL == mMode) {
                    // 回复邮件
                    strJson = Mail.replyMail(BBSWriteActivity.this, MailboxType.INBOX,
                            mMail.index, subject,
                            content);
                }
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (null != strError) {
                    // 是 错误信息
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                if (mAttachList.size() > 0) {
                    Article arti = new Article(strJson);
                    File file = null;
                    for (int i = 0; i < mAttachList.size(); i++) {
                        file = new File(mAttachList.get(i));
                        strError = Attachment.sendAttachment(BBSWriteActivity.this,
                                arti.board_name, arti.id, file);
                        strError = JsonHelper.checkError(strError);
                        if (null != strError) {
                            // 是 错误信息
                            if (mHandler != null) {
                                Message msg = mHandler.obtainMessage(MSG_ERROR);
                                Bundle data = msg.getData();
                                data.putString(KEY_DATA, strError);
                                mHandler.sendMessage(msg);
                            }
                            return;
                        }
                    }
                }
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_ARTICLE_SEND);
                }
            }
        });
    }

    /**
     * 结束
     * 
     * @param view
     */
    public void selfFinish(View view) {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage("确定退出吗？")
                .setOKButton(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        scrollToFinishActivity();
                    }
                }).setCancelButton(null).show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onChosenDir(String chosenDir) {
        mAttachList.add(chosenDir);

        // 判断附件是否超过5MB
        if (showAttachListSize() > 5) {
            mAttachList.remove(mAttachList.size() - 1);
            new CustomDialog(this)
                    .setMessage("附件最大为5MB\n目前附件个数：" + mAttachList.size())
                    .setOKButton(null).show();
            return;
        }

        ImageView iv = new ImageView(getBaseContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 72);
        iv.setLayoutParams(params);
        iv.setScaleType(ScaleType.CENTER_INSIDE);
        iv.setAdjustViewBounds(true);
        String path = chosenDir.toLowerCase();
        if (FileManager.isImage(path)) {
            Bitmap bmp = ImageFactory.getFixedBmp(path, BMP_WIDTH, BMP_HEIGHT, false);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
            }
        } else {
            int id;
            if (path.endsWith(".ppt") || path.endsWith(".pptx")) {
                id = R.drawable.file_ppt;
            } else if (path.endsWith(".doc") || path.endsWith(".docx")) {
                id = R.drawable.file_word;
            } else if (path.endsWith(".xls") || path.endsWith(".xlsx")) {
                id = R.drawable.file_xls;
            } else if (path.endsWith(".pdf")) {
                id = R.drawable.file_pdf;
            } else if (path.endsWith(".mp4") || path.endsWith(".mov")
                    || path.endsWith(".avi") || path.endsWith(".wmv")
                    || path.endsWith(".mkv") || path.endsWith(".flv")
                    || path.endsWith(".3gp") || path.endsWith(".rmvb")
                    || path.endsWith(".mpeg")) {
                id = R.drawable.file_vedio;
            } else if (path.endsWith(".mp3") || path.endsWith(".wav")
                    || path.endsWith(".wma") || path.endsWith(".amr")
                    || path.endsWith(".ogg")) {
                id = R.drawable.file_music;
            } else if (path.endsWith(".txt")) {
                id = R.drawable.file_txt;
            } else if (path.endsWith(".zip") || path.endsWith(".rar")) {
                id = R.drawable.file_zip;
            } else {
                id = R.drawable.file_file;
            }
            iv.setImageResource(id);
        }
        TextView tv = new TextView(getBaseContext());
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        tv.setLayoutParams(params);
        tv.setBackgroundColor(Color.TRANSPARENT);
        tv.setTextColor(Color.parseColor("#008800"));
        tv.setTextSize(20);
        tv.setGravity(Gravity.CENTER);
        tv.setText(mAttachList.size() + "");
        mAttachLLayout.addView(tv);
        mAttachLLayout.addView(iv);

        iv.setOnClickListener(new MyClickListener());
        if (Build.VERSION.SDK_INT >= 11) {
            iv.setOnTouchListener(new MyTouchListener());
        }
        Toast.makeText(getBaseContext(), chosenDir, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String picturePath = null;
            if (requestCode == RESULT_LOAD_IMAGE) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {
                        MediaStore.Images.Media.DATA
                };
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            } else if (requestCode == RESULT_TAKE_PHOTO) {
                picturePath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + FileManager.DIR_CAMERA
                        + "/"
                        + mTimeStamp + FileManager.BMP_SUFFIX;
                File file = new File(picturePath);
                if (!file.isFile()) {
                    picturePath = null;
                }
            }
            if (picturePath != null) {
                mAttachList.add(picturePath);

                // 判断附件是否超过5MB
                if (showAttachListSize() > 5) {
                    mAttachList.remove(mAttachList.size() - 1);
                    String strInfo = "附件最大为5MB\n目前附件个数：" + mAttachList.size()
                            + "\n总大小：" + String.format("%.2f", showAttachListSize()) + "MB";
                    new CustomDialog(this).setMessage(strInfo)
                            .setOKButton(null).show();
                    return;
                }

                ImageView iv = new ImageView(getBaseContext());
                LayoutParams params = new LayoutParams(
                        LayoutParams.WRAP_CONTENT, 72);
                iv.setLayoutParams(params);
                iv.setAdjustViewBounds(true);
                iv.setScaleType(ScaleType.CENTER_INSIDE);
                Bitmap bmp = ImageFactory.getFixedBmp(picturePath, BMP_WIDTH, BMP_HEIGHT, false);
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                }
                TextView tv = new TextView(getBaseContext());
                params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                tv.setLayoutParams(params);
                tv.setBackgroundColor(Color.TRANSPARENT);
                tv.setTextColor(Color.parseColor("#008800"));
                tv.setTextSize(20);
                tv.setGravity(Gravity.CENTER);
                tv.setText(mAttachList.size() + "");
                mAttachLLayout.addView(tv);
                mAttachLLayout.addView(iv);

                iv.setOnClickListener(new MyClickListener());
                if (Build.VERSION.SDK_INT >= 11) {
                    iv.setOnTouchListener(new MyTouchListener());
                }
                Toast.makeText(getBaseContext(), picturePath,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MyClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            mDragBmp = ((BitmapDrawable) ((ImageView) view).getDrawable())
                    .getBitmap();
            int position = mAttachLLayout.indexOfChild(view);
            position = (position + 1) / 2;
            String imageName = "[upload=" + position + "][/upload]";
            BBSManager.addPic(getBaseContext(), mContentET, mDragBmp, imageName,
                    true);
        }
    }

    /**
     * 附件的OnTouchListener
     * 
     * @author sollian
     */
    private class MyTouchListener implements OnTouchListener {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @SuppressWarnings("deprecation")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            float y = 0;
            int location[] = new int[2];
            // 获取控件在窗口中的绝对位置，不包括最顶部的状态栏
            view.getLocationInWindow(location);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    y = event.getY();

                    mDragBmp = ((BitmapDrawable) ((ImageView) view).getDrawable())
                            .getBitmap();
                    mDragView.setX(location[0]);
                    mDragView.setY(location[1] - mIUMgr.dip2px(50)
                            - mDragBmp.getHeight());
                    mDragView.setImageBitmap(mDragBmp);
                    mDragView.setAlpha(150);
                    mDragView.bringToFront();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mDragView.setX(location[0]);
                    mDragView.setY(location[1] + event.getY() - mIUMgr.dip2px(50)
                            - mDragBmp.getHeight());

                    int position = mAttachLLayout.indexOfChild(view);
                    if (Math.abs(y - event.getY()) > mIUMgr.dip2px(150)) {
                        // 向下拖动100dp的距离即删除附件
                        if (position > -1
                                && position < mAttachLLayout.getChildCount()) {
                            mAttachLLayout.removeView(view);
                            mAttachLLayout.removeViewAt(position - 1);
                            position = (position - 1) / 2;
                            mAttachList.remove(position);

                            Editable edit = mContentET.getText();
                            String etContent = edit.toString();
                            String delStr = "[upload=" + (position + 1)
                                    + "][/upload]";
                            int start = etContent.indexOf(delStr);
                            if (start != -1) {
                                edit.delete(start, start + delStr.length());
                            }
                            if (position < mAttachList.size()) {
                                String oldName = null;
                                String newName = null;
                                for (; position < mAttachList.size(); position++) {
                                    mDragBmp = ((BitmapDrawable) ((ImageView) mAttachLLayout
                                            .getChildAt(2 * position + 1))
                                            .getDrawable()).getBitmap();
                                    oldName = "[upload=" + (position + 2)
                                            + "][/upload]";
                                    newName = "[upload=" + (position + 1)
                                            + "][/upload]";
                                    etContent = edit.toString();// 更新et_Content
                                    start = etContent.indexOf(oldName);

                                    if (start != -1) {
                                        ImageSpan imageSpan = new ImageSpan(
                                                getBaseContext(), mDragBmp);
                                        SpannableString spannableString = new SpannableString(
                                                newName);
                                        spannableString
                                                .setSpan(
                                                        imageSpan,
                                                        0,
                                                        spannableString.length(),
                                                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        edit.replace(start,
                                                start + oldName.length(),
                                                spannableString);
                                    }
                                }
                            }
                            TextView tv = null;
                            for (int i = 0; i < mAttachLLayout.getChildCount(); i++) {
                                if (i % 2 == 0) {
                                    tv = (TextView) mAttachLLayout.getChildAt(i);
                                    tv.setText(1 + i / 2 + "");
                                }
                            }

                            mDragView.setImageBitmap(null);
                            if (mDragBmp != null) {
                                mDragBmp = null;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mDragView.setImageBitmap(null);
                    if (mDragBmp != null) {
                        mDragBmp = null;
                    }
                    break;
            }
            return false;
        }

    }

    private double showAttachListSize() {
        double size = 0;

        FileInputStream fis = null;
        File file = null;
        for (int i = 0; i < mAttachList.size(); i++) {
            file = new File(mAttachList.get(i));
            if (file.exists()) {
                try {
                    fis = new FileInputStream(file);
                    size += fis.available();
                } catch (Exception e) {

                }
            } else {
                Toast.makeText(getBaseContext(), "第" + i + "个文件不存在，请检查",
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (fis != null) {
            try {
                fis.close();
                fis = null;
            } catch (IOException e) {
            }
        }
        String message = null;
        if (size < 1024) {
            message = "附件大小：" + size + "B";
        } else if (size / 1024 < 1024) {
            message = "附件大小：" + String.format("%.2f", size / 1024) + "kB";
        } else if (size / 1024 / 1024 < 1024) {
            message = "附件大小：" + String.format("%.2f", size / 1024 / 1024)
                    + "MB";
        } else {
            message = "附件大小："
                    + String.format("%.2f", size / 1024 / 1024 / 1024) + "GB";
        }
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
        return size / 1024 / 1024;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDirChooserView.onKeyDown(keyCode, event)) {
            return true;
        }
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (View.VISIBLE == mHelpIV.getVisibility()) {
                mHelpIV.setVisibility(View.GONE);
                return true;
            }
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置cpb_progress的状态和是否显示
     * 
     * @param flag
     */
    private void showProgress(boolean flag) {
        if (flag) {
            mProgressFLayout.setVisibility(View.VISIBLE);
            mProgressBar.start();
        } else {
            mProgressFLayout.setVisibility(View.GONE);
            mProgressBar.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mArticle = null;
        mBoard = null;
        mMail = null;
        mTabList.clear();
        mTabList = null;
        // mAttachList.clear();
        // mAttachList = null;
        mDragView = null;
        if (null != mDragBmp && !mDragBmp.isRecycled()) {
            mDragBmp.recycle();
        }
        mDragBmp = null;
        mColorDialog = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        System.gc();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // 禁用滑动结束Activity
        getSwipeBackLayout().setEnableGesture(false);
        // 震动
        mIUMgr.vibrate(300);
        // 获取viewpager的屏幕坐标
        mViewPager.getLocationInWindow(vpLocation);
        // 获取可见的gif图位置、名称
        mDynamicFaceList.clear();

        int start = parent.getFirstVisiblePosition();
        int end = parent.getLastVisiblePosition();
        View v = null;
        for (int i = 0; i <= end - start; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            v = parent.getChildAt(i);
            int location[] = new int[2];
            if (null == v) {
                continue;
            }
            v.getLocationInWindow(location);
            if (v == view) {
                mDynamicFaceIVE.setId(i);
            }
            map.put("x", location[0] + "");
            map.put("y", location[1] + "");
            map.put("imgName", parent.getItemAtPosition(i + start).toString());
            mDynamicFaceList.add(map);
        }

        // 锁定vp，使不能滚动
        mViewPager.setScrollable(false);
        // 获取图片名称
        String imgName = parent.getItemAtPosition(position).toString();
        mDynamicFaceIVE.setSource(Converters.assetToByteArray(getAssets(),
                "face/" + imgName));
        // 获取控件在窗口中的绝对位置，不包括最顶部的状态栏
        int location[] = new int[2];
        view.getLocationInWindow(location);
        mDynamicFaceLLayout.setX(location[0] - mIUMgr.dip2px(10));
        mDynamicFaceLLayout.setY(location[1] - mIUMgr.dip2px(120));
        mDynamicFaceLLayout.setVisibility(View.VISIBLE);
        // 返回true可禁止gridview滚动
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mViewPager.getScrollable()) {
                    return false;
                }
                // 获取触摸点相对于屏幕的坐标
                float eventX = vpLocation[0] + event.getX();
                float eventY = vpLocation[1] + event.getY();

                int x = 0,
                y = 0;
                String imgName = null;
                for (int i = 0; i < mDynamicFaceList.size(); i++) {
                    HashMap<String, String> map = mDynamicFaceList.get(i);
                    x = Integer.parseInt(map.get("x"));
                    y = Integer.parseInt(map.get("y"));
                    if (eventX >= x && eventX <= x + mIUMgr.dip2px(40) && eventY >= y
                            && eventY <= y + mIUMgr.dip2px(40)) {
                        if (i == mDynamicFaceIVE.getId()) {
                            // 如果是正在播放的gif则返回
                            return false;
                        }
                        // 更新显示的gif
                        imgName = map.get("imgName").toString();
                        mDynamicFaceIVE.setSource(Converters.assetToByteArray(
                                getAssets(), "face/" + imgName));
                        mDynamicFaceIVE.setId(i);
                        mDynamicFaceLLayout.setX(x - mIUMgr.dip2px(10));
                        mDynamicFaceLLayout.setY(y - mIUMgr.dip2px(120));
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 重新初始化滑动结束Activity
                initSwipeOut();
                mViewPager.setScrollable(true);
                mDynamicFaceLLayout.setVisibility(View.GONE);
                break;
        }
        return false;
    }
}
