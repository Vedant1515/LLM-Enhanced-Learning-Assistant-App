package com.learningassistant.app.adapters;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;
import com.learningassistant.app.R;
import java.util.ArrayList;
import java.util.List;

public class InterestTagAdapter {

    private final Context context;
    private final FlexboxLayout container;
    private final List<String> allTags;
    private final List<String> selectedTags = new ArrayList<>();
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public InterestTagAdapter(Context context, FlexboxLayout container, List<String> tags) {
        this.context = context;
        this.container = container;
        this.allTags = tags;
        buildTags();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    private void buildTags() {
        container.removeAllViews();
        for (String tag : allTags) {
            Button btn = new Button(context);
            btn.setText(tag);
            btn.setTextSize(13f);
            btn.setAllCaps(false);
            btn.setBackground(context.getDrawable(R.drawable.btn_tag_unselected));
            btn.setTextColor(context.getColor(R.color.colorTextMuted));

            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(6, 6, 6, 6);
            btn.setLayoutParams(lp);

            int paddingH = (int) (14 * context.getResources().getDisplayMetrics().density);
            int paddingV = (int) (8 * context.getResources().getDisplayMetrics().density);
            btn.setPadding(paddingH, paddingV, paddingH, paddingV);

            btn.setOnClickListener(v -> toggleTag(btn, tag));
            container.addView(btn);
        }
    }

    private void toggleTag(Button btn, String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
            btn.setBackground(context.getDrawable(R.drawable.btn_tag_unselected));
            btn.setTextColor(context.getColor(R.color.colorTextMuted));
        } else {
            if (selectedTags.size() >= 10) {
                Toast.makeText(context, context.getString(R.string.error_max_interests),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTags.add(tag);
            btn.setBackground(context.getDrawable(R.drawable.btn_tag_selected));
            btn.setTextColor(context.getColor(R.color.colorPrimary));
        }
        if (listener != null) {
            listener.onSelectionChanged(selectedTags.size());
        }
    }

    public List<String> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }
}
