package com.posun.lightui.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * package light3:com.posun.lightui.recyclerview.LightDefultLayoutManager.class
 * 作者：zyq on 2018/1/31 11:16
 * 邮箱：zyq@posun.com
 */

public class LightDefultLayoutManager extends RecyclerView.LayoutManager {
    private int totalHeight = 0, totalWight = 0;
    private int spanCount = 3, beforSpan = 0, afterSpan = 0, JUMP_LOOP = -100;
    private LightScrollBarHelper mLightScrollBarHelper;
    int itemCount = 0;
    private Context context;
    private LightGroupRecycler groupRecycler;
    private boolean isGroupAdapter = true;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    public LightDefultLayoutManager(Context context) {
        this.context = context;
    }


    private SpanSizeLookup mSpanSizeLookup;

    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        mSpanSizeLookup = spanSizeLookup;
    }

    public LightDefultLayoutManager(Context context, int spanCount) {
        this.spanCount = spanCount;
        this.context = context;
    }

    private int getItemSpan(int position) {
        if (mSpanSizeLookup != null) {
            return mSpanSizeLookup.getSpanSize(position);
        }
        return 1;
    }

    boolean initScrollBars = false;

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中
        detachAndScrapAttachedViews(recycler);
        if (groupRecycler == null)
            groupRecycler = new LightGroupRecycler(this);
        beforSpan = 0;
        afterSpan = 0;
        itemCount = getItemCount();
        //定义竖直方向的偏移量
        int offsetY = 0;
        totalHeight = getHeight();
        totalWight = getWidth();
        ItemIndex index = new ItemIndex(0);
        while (offsetY < totalHeight && haveNext(index.getIndex())) {
            int height = addBeforItemWithGroup(recycler, index, offsetY);
            offsetY += height;
        }
        double initScrollPosition = index.getIndex() / (double) groupRecycler.getRecyclerView().getAdapter().getItemCount();
        initScrollPosition = initScrollPosition * 100;
        if (mLightScrollBarHelper == null) {
            mLightScrollBarHelper = new LightScrollBarHelper();
        }
        mLightScrollBarHelper.setInitScrollPosition(initScrollPosition);
    }


    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        dy = fillLayout(dy, recycler);
        return dy;
    }

    private int fillLayout(int dy, RecyclerView.Recycler recycler) {
        Log.e("fillLayout", groupRecycler.getRecyclerView().getChildCount() + "");
        if (dy > 0) {
            View lastView = getLastView();
            int addheight = (int) (lastView.getY() + lastView.getMeasuredHeight());
            addheight = addheight - totalHeight;
            int offsetY = (int) lastView.getY() + lastView.getMeasuredHeight();
            resitLastViewSpan(lastView);
            ItemIndex index = new ItemIndex(getPosition(lastView) + 1);
            while (haveNext(index.getIndex()) && addheight <= dy) {
                int height = addBeforItemWithGroup(recycler, index, offsetY);
                offsetY += height;
                addheight += height;
            }
            if (dy > addheight) {
                dy = addheight;
            }
            offsetChildrenVertical(-dy);
            recyclerView(true, recycler);
        } else {
            View fistView = getFistView();
            int addheight = 0;
            int offsetY = (int) fistView.getY();
            if (offsetY < 0) {
                addheight = (int) Math.abs(fistView.getY());
            }
            ItemIndex index = new ItemIndex(getPosition(fistView) - 1);
            resitfistViewSpan(fistView);
            int juageheight = juageHasGroupView(fistView, offsetY, index.getIndex() + 1);
            addheight += juageheight;
            offsetY -= juageheight;
            while (index.getIndex() >= 0 && addheight <= Math.abs(dy)) {
                int height = addAfterItemWithGroup(recycler, index, offsetY);
                if (height == JUMP_LOOP) {
                    break;
                } else {
                    offsetY -= height;
                    addheight += height;
                }
            }
            if (Math.abs(dy) > addheight) {
                dy = -addheight;
            }
            offsetChildrenVertical(-dy);
            recyclerView(false, recycler);
        }
        return dy;
    }

    private int juageHasGroupView(View fistview, int offsetY, int position) {
        int addheight = 0;
        if (groupRecycler.getGroupAdapter().isGroup(position)) {
            if (!groupRecycler.isGroupView(fistview)) {
                View view = groupRecycler.getGroupViewForPosition(position);
                measureChild(view, 0, 0);
                groupRecycler.addViewToRecycleView(view, 0, this, true);
                int viewHeight = view.getMeasuredHeight();
                layoutDecoratedWithMargins(view, 0, offsetY - viewHeight, totalHeight, offsetY);
                addheight += viewHeight;
                afterSpan = groupRecycler.getGroupUpperSpan(position, spanCount);
            }
        }
        return addheight;
    }

    public View getFistView() {
        View adapterView = getChildAt(0);
        if (!isGroupAdapter) {
            return adapterView;
        }
        View groupView = groupRecycler.getGroupViewAt(0);
        if (groupView == null)
            return adapterView;
        return groupView.getY() < adapterView.getY() ? groupView : adapterView;
    }

    public View getLastView() {
        View adapterView = getChildAt(getChildCount() - 1);
        if (!isGroupAdapter) {
            return adapterView;
        }
        View groupView = groupRecycler.getGroupViewAt(groupRecycler.getGroupViewCount() - 1);
        if (groupView == null)
            return adapterView;
        return groupView.getY() > adapterView.getY() ? groupView : adapterView;
    }

    @Override
    public int getPosition(View view) {
        if (groupRecycler.isGroupView(view)) {
            return groupRecycler.getPositionFromGroupView(view);
        }
        return super.getPosition(view);
    }

    /***
     * 向上滑动时加载底部View重置底部加载坐标位置
     * @param view
     */
    private void resitLastViewSpan(View view) {
        if (groupRecycler.isGroupView(view)) {
            beforSpan = 0;
            return;
        }
        int span = getItemSpan(getPosition(view));
        int x = (int) view.getX();
        beforSpan = (x / (totalWight / spanCount)) + span;
    }

    /**
     * 向下滑动时加载顶部View重置顶部加载坐标位置
     *
     * @param view
     */
    private void resitfistViewSpan(View view) {//这里有问题
        int x = totalWight - ((int) view.getX());//向下滑动时视图为逆序添加
        afterSpan = spanCount - (x / (totalWight / spanCount));
    }

    private int addAfterItemWithGroup(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        if (index.getIndex() < 0)
            return JUMP_LOOP;
        int position = index.getIndex();
        if (isGroupAdapter && (position == -1 || (groupRecycler.getGroupAdapter().isGroup(position)))) {
            int height = 0;
            height += addAfterItem(recycler, index, offsetY);//渲染子视图
            afterSpan = groupRecycler.getGroupUpperSpan(position, spanCount);
            View fistview = getChildAt(0);
            offsetY -= fistview.getMeasuredHeight();
            View view = groupRecycler.getGroupViewForPosition(position);
            measureChild(view, 0, 0);
            groupRecycler.addViewToRecycleView(view, 0, this, true);
            int viewHeight = view.getMeasuredHeight();
            layoutDecoratedWithMargins(view, 0, offsetY - viewHeight, totalHeight, offsetY);
            return height + viewHeight + addAfterItem1(recycler, index, offsetY - viewHeight);
        }
        return addAfterItem(recycler, index, offsetY);
    }


    private int addAfterItem1(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        int height = 0;
        if (afterSpan == spanCount) {//防止while循环不被执行
            afterSpan = 0;
        }
        while (index.getIndex() >= 0 && afterSpan != spanCount) {//渲染多一行子view因为groupView一直会在队列的顶部
            height += addAfterItem(recycler, index, offsetY);
        }
        return height;
    }

    private int addAfterItem(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        View view = recycler.getViewForPosition(index.getIndex());
        groupRecycler.addViewToRecycleView(view, 0, this, false);
        int span = getItemSpan(index.getIndex());
        if (afterSpan >= spanCount) {
            afterSpan = 0;
        }
        int item = totalWight / spanCount;
        int wightUsed = item * (spanCount - span);
        measureChild(view, wightUsed, 0);
        int width = getDecoratedMeasuredWidth(view);
        int height = getDecoratedMeasuredHeight(view);
        int right = totalWight - (afterSpan * item);
        int left = right - width;
        layoutDecoratedWithMargins(view, left, offsetY - height, right, offsetY);
        index.previous();
        if ((afterSpan + span) < spanCount) {
            afterSpan += span;
            return 0;
        } else {
            afterSpan += span;
            return height;
        }
    }

    private int addBeforItemWithGroup(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        int position = index.getIndex();
        if (isGroupAdapter && groupRecycler.getGroupAdapter().isGroup(position)) {
            int otherLine = 0;
            if (beforSpan != 0 && spanCount != 1) {
                View view = getChildAt(getChildCount() - 1);
                otherLine = view.getMeasuredHeight();
                groupRecycler.addGroupUpperSpan(position, beforSpan);
            }
            View view = groupRecycler.getGroupViewForPosition(position);
            measureChild(view, 0, 0);
            groupRecycler.addViewToRecycleView(view, this, true);
            int height = view.getMeasuredHeight();
            layoutDecoratedWithMargins(view, 0, offsetY + otherLine, totalHeight, offsetY + height + otherLine);
            beforSpan = 0;
            return height + otherLine + addBeforItem1(recycler, index, offsetY + height + otherLine);
        }
        return addBeforItem(recycler, index, offsetY);
    }

    private int addBeforItem1(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        int height = 0;
        while (beforSpan != spanCount && !groupRecycler.getGroupAdapter().isGroup(index.getIndex() + 1)) {
            height += addBeforItem(recycler, index, offsetY);
        }
        return height;
    }

    private int addBeforItem(RecyclerView.Recycler recycler, ItemIndex index, int offsetY) {
        Log.e("addBeforItemWithGroup", index.toString());
        View view = recycler.getViewForPosition(index.getIndex());
        int span = getItemSpan(index.getIndex());
        if (beforSpan >= spanCount) {
            beforSpan = 0;
        }
        int item = totalWight / spanCount;
        int wightUsed = item * (spanCount - span);
        measureChild(view, wightUsed, 0);
        int width = getDecoratedMeasuredWidth(view);
        int height = getDecoratedMeasuredHeight(view);
        int left = beforSpan * item;
        groupRecycler.addViewToRecycleView(view, this, false);
        index.next();
        int right = width + left;
        layoutDecoratedWithMargins(view, left, offsetY, right, offsetY + height);
        if ((beforSpan + span) < spanCount) {
            beforSpan += span;
            return 0;
        } else {
            beforSpan += span;
            return height;
        }
    }

    private boolean haveNext(int position) {
        return position < itemCount;
    }

    /**
     * 回收屏幕外面的视图
     *
     * @param up
     * @param recycler
     */
    private void recyclerView(boolean up, RecyclerView.Recycler recycler) {
        groupRecycler.recyclerOutScreenView(up, totalHeight, this);
        groupRecycler.removeAndRecycleView(this, recycler);
    }

    public interface SpanSizeLookup {
        int getSpanSize(int position);
    }

    @Override
    public void offsetChildrenVertical(int dy) {
        int size = groupRecycler.getRecyclerView().getChildCount();
        for (int i = 0; i < size; i++) {
            View view = groupRecycler.getRecyclerView().getChildAt(i);
            view.offsetTopAndBottom(dy);
        }
    }


//    private int getHorizontalSpace() {
//        return getWidth() - getPaddingLeft() - getPaddingRight();
//    }
//
//    private int getVerticalSpace() {
//        return getHeight() - getPaddingTop() - getPaddingBottom();
//    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return mLightScrollBarHelper.MAX * mLightScrollBarHelper.ENLARGE;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return (int) (mLightScrollBarHelper.ENLARGE * mLightScrollBarHelper.getInitScrollPosition());
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        View view = getChildAt(getChildCount() - 1);
        int position = getPosition(view);
        return mLightScrollBarHelper.verticalScroll(position, state, totalHeight - (int) view.getY(), view.getHeight());
    }

    private class ItemIndex {
        private int num;

        public ItemIndex(int num) {
            this.num = num;
        }

        public int getIndex() {
            return num;
        }

        public void next() {
            num++;
        }

        public void previous() {
            num--;
        }

        @Override
        public String toString() {
            return String.valueOf(num);
        }
    }


}
