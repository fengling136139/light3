package com.posun.lightui.richView;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.posun.lightui.richView.annotation.LightBtnItem;
import com.posun.lightui.richView.annotation.LightCheckBox;
import com.posun.lightui.richView.annotation.LightItemsGroups;
import com.posun.lightui.richView.annotation.LightRichUI;
import com.posun.lightui.richView.annotation.LightSelect;
import com.posun.lightui.richView.annotation.LightSimpleClick;
import com.posun.lightui.richView.annotation.LightTextLab;
import com.posun.lightui.richView.instent.EventBean;
import com.posun.lightui.richView.instent.SimpleClickExeCute;
import com.posun.lightui.richView.view.LightBtnItemView;
import com.posun.lightui.richView.view.LightCheckGroup;
import com.posun.lightui.richView.view.LightItemGroupView;
import com.posun.lightui.richView.view.LightLinearLayout;
import com.posun.lightui.richView.view.LightSelectGroup;
import com.posun.lightui.richView.view.LightTextInputView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qing on 2018/1/11.
 */

public abstract class LightRichActivityManager {
    Map<LightUINB, LinearLayout> rootViewMap = new HashMap<>();
    private Object clazzobj;
    private List<ViewBean> views;
    private Map<Integer, LightItemGroupInterface> viewGroupCatch = new HashMap<>();

    public Object getDataobj() {
        return clazzobj;
    }

    /**
     * @param clazzobj
     * @param context
     * @throws Exception
     */
    public void initUIData(Object clazzobj, Activity context) throws Exception {
        this.clazzobj = clazzobj;
        Class clazz = clazzobj.getClass();

        Field[] fields = clazz.getDeclaredFields();
        if (views == null)
            views = new ArrayList<>();
        views.clear();
        praseDataForUI(clazzobj, context, fields);
        Collections.sort(views, new ViewComparable());
        addItemView(context);
        for (LightUINB itemLightUINB : rootViewMap.keySet()) {
            if (itemLightUINB == LightUINB.ONE)/////
                AddRichView(rootViewMap.get(itemLightUINB));
        }
    }

    /***
     *
     * @param clazzobj
     * @param context
     * @param fields
     * @throws Exception
     */
    private void praseDataForUI(Object clazzobj, Activity context, Field[] fields) throws Exception {
        for (Field item : fields) {
            item.setAccessible(true);
            LightItemIntface itemView = null;
            LightRichUI mLightRichUI = item.getAnnotation(LightRichUI.class);
            if (mLightRichUI == null)
                continue;
            itemView = getLightView(clazzobj, context, item, itemView, mLightRichUI);
            views.add(new ViewBean(itemView, mLightRichUI));
        }
    }


    private class ViewComparable implements Comparator<ViewBean> {

        @Override
        public int compare(ViewBean viewBean, ViewBean t1) {
            return viewBean.lightUINB.order() - t1.lightUINB.order();
        }
    }

    /**
     * @param context
     */
    private void addItemView(Context context) {
        LinearLayout typerootView = null;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LightItemGroupInterface itemsGroup = null;
        LinearLayout.LayoutParams grouplayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < views.size(); i++) {
            ViewBean viewBean = views.get(i);
            typerootView = rootViewMap.get(viewBean.getLightUINB().value());
            if (typerootView == null) {
                typerootView = new LightLinearLayout(context);
                typerootView.setOrientation(LinearLayout.VERTICAL);
                rootViewMap.put(viewBean.getLightUINB().value(), typerootView);
            }
            if (itemsGroup != null && itemsGroup.getEndOrder() > i) {
                itemsGroup.getViewGroup().addView(viewBean.lightItemIntface.getMyView());
            } else if (itemsGroup != null && itemsGroup.getEndOrder() == i) {
                itemsGroup.getViewGroup().addView(viewBean.lightItemIntface.getMyView());
                typerootView.addView(itemsGroup.getViewGroup(), grouplayoutParams);
                itemsGroup = null;
            } else if (itemsGroup == null && viewGroupCatch.containsKey(i)) {
                itemsGroup = viewGroupCatch.get(i);
                typerootView.addView(itemsGroup.getTitleView());
                itemsGroup.getViewGroup().addView(viewBean.lightItemIntface.getMyView());
                if (itemsGroup.getEndOrder() == i) {
                    typerootView.addView(itemsGroup.getViewGroup(), grouplayoutParams);
                    itemsGroup = null;
                }
            } else {
                typerootView.addView(viewBean.lightItemIntface.getMyView(), layoutParams);
            }
        }
    }

    /**
     * @param clazzobj
     * @param context
     * @param item
     * @param itemView
     * @return
     * @throws Exception
     */
    private LightItemIntface getLightView(Object clazzobj, Context context, Field item, LightItemIntface itemView, LightRichUI mLightRichUI) throws Exception {
        Annotation[] annotations = item.getDeclaredAnnotations();
        for (Annotation obj : annotations) {

            if (obj instanceof LightTextLab) {
                LightTextLab mLightTextLab = (LightTextLab) obj;
                LightTextInputView mLightTextGroup = new LightTextInputView(context, mLightTextLab.lab(), String.valueOf(item.get(clazzobj)));
                itemView = mLightTextGroup;
            } else if (obj instanceof LightBtnItem) {
                LightBtnItem mLightBtnItem = (LightBtnItem) obj;
                itemView = new LightBtnItemView(context, mLightBtnItem.lab());
            } else if (obj instanceof LightCheckBox) {
                LightCheckBox mLightCheckBox = (LightCheckBox) obj;
                itemView = new LightCheckGroup(context, mLightCheckBox.lab(), mLightCheckBox.value());
            } else if (obj instanceof LightItemsGroups) {
                LightItemsGroups lightitemsgroups = (LightItemsGroups) obj;
                LightItemGroupView mLightItemGroupView = new LightItemGroupView(mLightRichUI.order(), lightitemsgroups.end(), lightitemsgroups.labename(), context);
                viewGroupCatch.put(mLightRichUI.order(), mLightItemGroupView);
            } else if (obj instanceof LightSelect) {
                LightSelect mLightSelect = (LightSelect) obj;
                itemView = new LightSelectGroup(context, mLightSelect.lab(), String.valueOf(item.get(clazzobj)));
            }
            praseEvent(itemView, obj);
        }
        if (item != null)
            itemView.setField(item);
        return itemView;
    }

    private void praseEvent(LightItemIntface itemView, Annotation obj) {
        if (obj instanceof LightSimpleClick) {
            LightSimpleClick mLightSimpleClick = (LightSimpleClick) obj;
            LightActionExeCute mLightActionExeCute = new SimpleClickExeCute(new EventBean(mLightSimpleClick.type(), mLightSimpleClick.value()));
            itemView.getMyView().setTag(mLightActionExeCute);
            if (clickListener == null)
                clickListener = new LightOnclick();
            itemView.getMyView().setOnClickListener(clickListener);
        }
    }

    private View.OnClickListener clickListener;

    private class LightOnclick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            LightActionExeCute mLightActionExeCute = (LightActionExeCute) view.getTag();
            mLightActionExeCute.execute((LightItemIntface) view, LightRichActivityManager.this);
        }
    }

    public void commitAllData() {
        for (ViewBean itemIntface : views) {
            itemIntface.getLightItemIntface().saveValue(clazzobj);
        }
    }

    private static class ViewBean {
        private LightItemIntface lightItemIntface;
        private LightRichUI lightUINB;

        /***
         *
         * @param lightItemIntface
         * @param lightUINB
         */
        private ViewBean(LightItemIntface lightItemIntface, LightRichUI lightUINB) {
            this.lightItemIntface = lightItemIntface;
            this.lightUINB = lightUINB;
        }

        public LightItemIntface getLightItemIntface() {
            return lightItemIntface;
        }

        public void setLightItemIntface(LightItemIntface lightItemIntface) {
            this.lightItemIntface = lightItemIntface;
        }

        public LightRichUI getLightUINB() {
            return lightUINB;
        }

        public void setLightUINB(LightRichUI lightUINB) {
            this.lightUINB = lightUINB;
        }
    }

    /**
     * @param view
     */
    public abstract void AddRichView(View view);


    public interface LightItemIntface {
        Field getItemField();

        View getMyView();

        void saveValue(Object object);

        void setField(Field field);
    }

    public interface LightItemGroupInterface {
        View getTitleView();

        ViewGroup getViewGroup();

        int getEndOrder();
    }
}
