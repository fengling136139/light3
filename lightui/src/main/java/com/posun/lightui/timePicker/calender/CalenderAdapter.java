package com.posun.lightui.timePicker.calender;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

public class CalenderAdapter extends BasePagerAdapter {
    private DateTime startDate = DateTime.now();
    private int mCurrentIndex = Integer.MAX_VALUE / 2;
    private DateTime selectDate;
    private ViewPager viewPager;
    private LightCalenderView.LightListener listener;
    private boolean ismonth = true;

    public void setIsmonth(boolean ismonth) {
        validataAll(ismonth);
        this.ismonth = ismonth;
    }

    private void validataAll(boolean arg) {
        DateTime changedateTime = startDate;
        int size = viewPager.getChildCount();
        for (int i = 0; i < size; i++) {
            BaseCalenderView item = (BaseCalenderView) viewPager.getChildAt(i);
            item.setIsmonth(arg);
            resitDate(item, changedateTime, arg);
        }
        if (viewcatch != null)
            for (View view : viewcatch) {
                BaseCalenderView item = (BaseCalenderView) view;
                item.setIsmonth(arg);
                resitDate(item, changedateTime, arg);
            }
    }

    private void resitDate(BaseCalenderView item, DateTime dateTime, boolean newisMonth) {
        int cha = 0;
        if (ismonth) {
            cha = item.getDateTime().getMonthOfYear() - dateTime.getMonthOfYear();
        } else {
            cha = item.getDateTime().getDayOfWeek() - dateTime.getDayOfWeek();
        }
        if (newisMonth) {
            item.validate(dateTime.plusMonths(cha), selectDate);
        } else {
            item.validate(dateTime.plusWeeks(cha), selectDate);
        }
    }

    public CalenderAdapter(ViewPager viewPager, boolean ismonth) {
        this.ismonth = ismonth;
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > mCurrentIndex) {
                    swipeForward(1);
                } else if (position < mCurrentIndex) {
                    swipeForward(-1);
                }
                mCurrentIndex = position;
                if (CalenderAdapter.this.listener != null)
                    CalenderAdapter.this.listener.DateChange(startDate);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public LightCalenderView.LightListener getListener() {
        return listener;
    }

    public void setListener(LightCalenderView.LightListener listener) {
        this.listener = listener;
    }

    @Override
    protected View getView(ViewGroup container, View contentView, int position) {
        BaseCalenderView baseCalenderView = null;
        DateTime dateTime = null;
        if (position != mCurrentIndex) {
            if (ismonth) {
                dateTime = startDate.plusMonths(position - mCurrentIndex);
            } else {
                dateTime = startDate.plusWeeks(position - mCurrentIndex);
            }
        } else {
            dateTime = startDate;
        }
        if (contentView != null) {
            baseCalenderView = (BaseCalenderView) contentView;
            baseCalenderView.validate(dateTime, selectDate);
        } else {
            baseCalenderView = new BaseCalenderView(container.getContext(), dateTime);
            baseCalenderView.setIsmonth(ismonth);
            baseCalenderView.setListener(selectListener);
        }
        return baseCalenderView;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public void swipeForward(int i) {
        if (ismonth) {
            startDate = startDate.plusMonths(i);
        } else {
            startDate = startDate.plusWeeks(i);
        }

    }

    BaseCalenderView.SelectListener selectListener = new BaseCalenderView.SelectListener() {
        @Override
        public void select(BaseCalenderView view, int item, DateTime selectDate, int cha) {
            Log.e("TAG", selectDate.toString("yyyy-MM-dd"));
            if (ismonth && cha != 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + cha);
            }
            int size = viewPager.getChildCount();
            /**刷新视图缓存*/
            for (int i = 0; i < size; i++) {
                BaseCalenderView catch_item = (BaseCalenderView) viewPager.getChildAt(i);
                if (isSameView(selectDate, catch_item)) {
                    catch_item.validate(selectDate);
                } else {
                    catch_item.resetSelect(-1);
                }
            }
            if (listener != null)
                listener.select(selectDate);
            CalenderAdapter.this.selectDate = selectDate;
        }
    };

    private boolean isSameView(DateTime selectDate, BaseCalenderView catch_item) {
        if (!ismonth) {
            DateTime dateTime = catch_item.getDateTime().plusDays(0 - (catch_item.getDateTime().getDayOfWeek() % 7));
            return selectDate.getDayOfYear() - dateTime.getDayOfYear() < 7;
        }
        return selectDate.getYear() == catch_item.getDateTime().getYear() && selectDate.getMonthOfYear() == catch_item.getDateTime().getMonthOfYear();
    }
}