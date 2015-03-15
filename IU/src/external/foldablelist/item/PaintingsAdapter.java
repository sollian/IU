
package external.foldablelist.item;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.utils.SwitchManager;
import com.azcltd.fluffycommons.adapters.ItemsAdapter;
import com.azcltd.fluffycommons.utils.Views;

import external.SmartImageView.SmartImageView;

public class PaintingsAdapter extends ItemsAdapter<Painting> implements
        View.OnClickListener {

    public interface OpenDetailsListener {
        public void onOpenDetails(View v, Painting item);
    }

    private OpenDetailsListener listener;

    public PaintingsAdapter(Context context, ArrayList<Painting> list,
            OpenDetailsListener listener) {
        super(context);
        setItemsList(list);
        this.listener = listener;
    }

    @Override
    protected View createView(Painting item, int pos, ViewGroup parent,
            LayoutInflater inflater) {
        ViewHolder vh;
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_photoshow_list, parent, false);
        vh = new ViewHolder();
        vh.fl = Views.find(view, R.id.list_item_photoshow_fl);

        vh.siv = Views.find(view, R.id.list_item_photoshow_image);
        vh.siv.setOnClickListener(this);
        vh.tv_title = Views.find(view, R.id.list_item_photoshow_title);
        view.setTag(vh);
        return view;
    }

    @Override
    protected void bindView(Painting item, int pos, View convertView) {
        ViewHolder vh = (ViewHolder) convertView.getTag();
        int nX = 127;
        if (SwitchManager.getInstance(AiYouApplication.getInstance()).isNightModeEnabled()) {
            nX = 0;
            vh.tv_title.setTextColor(Color.GRAY);
        }
        int nR = (int) (Math.random() * 128 + nX);
        int nG = (int) (Math.random() * 128 + nX);
        int nB = (int) (Math.random() * 128 + nX);
        int color = Color.rgb(nR, nG, nB);
        vh.fl.setBackgroundColor(color);

        vh.siv.setTag(item);
        vh.siv.setImageUrl(item.getImageUrl(), R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
        vh.tv_title.setText(item.getTitle());
        item.setColor(color);
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onOpenDetails(view, (Painting) view.getTag());
        }

    }

    private static class ViewHolder {
        FrameLayout fl;
        SmartImageView siv;
        TextView tv_title;
    }

}
