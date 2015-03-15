
package com.aiyou.bbs.FileSelect;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.aiyou.R;
import com.aiyou.utils.SwitchManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryChooserView extends LinearLayout {
    private TextView mDirTextView;
    private Button mLastDirButton;
    private ListView mListView;
    private Context mContext;

    private String mSDCardDir;
    private String mDir = "";
    private ArrayList<String> mSubDirsList;
    private ChosenDirectoryListener mChosenDirListener;
    private ArrayAdapter<String> mListAdapter;
    
    private SwitchManager mSwitchMgr;

    public DirectoryChooserView(Context context) {
        super(context);
        init(context);
    }

    public DirectoryChooserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public DirectoryChooserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setChosenDirectoryListener(ChosenDirectoryListener listener) {
        mChosenDirListener = listener;
    }

    public void chooseDirectory() {
        chooseDirectory(mSDCardDir);
    }

    public void chooseDirectory(String dir) {
        setVisibility(View.VISIBLE);
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = mSDCardDir;
        }

        mDir = dir;
        mSubDirsList = getDirectories(dir);
        mListAdapter = createListAdapter(mSubDirsList);
        mListView.setAdapter(mListAdapter);
        updateDirectory();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && getVisibility() == View.VISIBLE) {
            setVisibility(View.GONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init(Context context) {
        mContext = context;
        mSwitchMgr = SwitchManager.getInstance(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.directory_chooser_view, this);
        mDirTextView = (TextView) view.findViewById(R.id.directory_chooser_view_tv);
        mLastDirButton = (Button) view.findViewById(R.id.directory_chooser_view_bt);
        mListView = (ListView) view.findViewById(R.id.directory_chooser_view_lv);
        mLastDirButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDir.equals(mSDCardDir)) {
                    // The very top level directory, do nothing
                    return;
                } else {
                    // Navigate back to an upper directory
                    mDir = new File(mDir).getParent();
                    updateDirectory();
                }
            }

        });
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDir += "/" + mSubDirsList.get(position);
                File file = new File(mDir);
                if (file.isDirectory()) {
                    updateDirectory();
                } else {
                    if (mChosenDirListener != null) {
                        mChosenDirListener.onChosenDir(mDir);
                    }
                    setVisibility(View.GONE);
                }
            }

        });

        mSDCardDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
    }

    private ArrayList<String> getDirectories(String dir) {
        ArrayList<String> dirs = new ArrayList<String>();
        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            String fileName = null;
            for (File file : dirFile.listFiles()) {
                fileName = file.getName();
                if (!fileName.startsWith(".")) {// 不显示隐藏文件
                    dirs.add(fileName);
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;
    }

    private void updateDirectory() {
        mSubDirsList.clear();
        mSubDirsList.addAll(getDirectories(mDir));
        mDirTextView.setText(mDir);

        mListAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(final ArrayList<String> items) {
        return new ArrayAdapter<String>(mContext,
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @SuppressWarnings("deprecation")
            @SuppressLint("DefaultLocale")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextSize(15);
                    if (mSwitchMgr.isNightModeEnabled()) {
                        tv.setTextColor(Color.GRAY);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                    String path = mDir + "/"
                            + items.get(position).toString().toLowerCase();
                    File file = new File(path);
                    int id = R.drawable.file_folder;
                    if (!file.isDirectory()) {
                        if (path.endsWith(".ppt") || path.endsWith(".pptx")) {
                            id = R.drawable.file_ppt;
                        } else if (path.endsWith(".doc")
                                || path.endsWith(".docx")) {
                            id = R.drawable.file_word;
                        } else if (path.endsWith(".xls")
                                || path.endsWith(".xlsx")) {
                            id = R.drawable.file_xls;
                        } else if (path.endsWith(".pdf")) {
                            id = R.drawable.file_pdf;
                        } else if (path.endsWith(".bmp")
                                || path.endsWith(".jpg")
                                || path.endsWith(".png")
                                || path.endsWith(".gif")
                                || path.endsWith(".jpeg")) {
                            id = R.drawable.file_img;
                        } else if (path.endsWith(".mp4")
                                || path.endsWith(".mov")
                                || path.endsWith(".avi")
                                || path.endsWith(".wmv")
                                || path.endsWith(".mkv")
                                || path.endsWith(".flv")
                                || path.endsWith(".3gp")
                                || path.endsWith(".rmvb")
                                || path.endsWith(".mpeg")) {
                            id = R.drawable.file_vedio;
                        } else if (path.endsWith(".mp3")
                                || path.endsWith(".wav")
                                || path.endsWith(".wma")
                                || path.endsWith(".amr")
                                || path.endsWith(".ogg")) {
                            id = R.drawable.file_music;
                        } else if (path.endsWith(".txt")) {
                            id = R.drawable.file_txt;
                        } else if (path.endsWith(".zip")
                                || path.endsWith(".rar")) {
                            id = R.drawable.file_zip;
                        } else {
                            id = R.drawable.file_file;
                        }
                    }
                    Drawable drawable = null;

                    if (id == R.drawable.file_img) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        Bitmap bmp = BitmapFactory.decodeFile(path, options);
                        int scalew = (int) (options.outWidth / 72.0);
                        int scaleh = (int) (options.outHeight / 72.0);
                        options.inSampleSize = Math.max(scalew, scaleh) + 1;
                        // 获取图像
                        options.inJustDecodeBounds = false;
                        options.inInputShareable = true;
                        options.inPurgeable = true;
                        bmp = BitmapFactory.decodeFile(path, options);
                        if (bmp != null) {
                            drawable = new BitmapDrawable(bmp);
                        }
                    }

                    if (drawable == null) {
                        drawable = mContext.getResources().getDrawable(id);
                    }
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                            drawable.getMinimumHeight());
                    if (mSwitchMgr.isNightModeEnabled()) {
                        drawable.setColorFilter(Color.GRAY, Mode.MULTIPLY);
                    }
                    tv.setCompoundDrawables(drawable, null, null, null);
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }

    public interface ChosenDirectoryListener {
        public void onChosenDir(String chosenDir);
    }

}
