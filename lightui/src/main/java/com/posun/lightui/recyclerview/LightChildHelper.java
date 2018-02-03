package com.posun.lightui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * package light3:com.posun.lightui.recyclerview.LightChildHelper.class
 * 作者：zyq on 2018/2/2 15:30
 * 邮箱：zyq@posun.com
 */

public class LightChildHelper {
    private Object object;
    Class ChildHelperClass;
    Map<String,Object> catchFect=new HashMap<>();
    public LightChildHelper(RecyclerView.LayoutManager layoutManager) {
        getChildHelper(layoutManager);
        try {
            ChildHelperClass=Class.forName("android.support.v7.widget.ChildHelper");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getChildHelper(Object object) {
        try {
            String name = "mChildHelper";
            Field field = RecyclerView.LayoutManager.class.getDeclaredField(name);
            field.setAccessible(true);
            this.object = field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addView(View child, int index, boolean hidden) {
        String name = "addView";
        String catchName=name+"3";
        Method method = null;
        try {
            if(catchFect.containsKey(catchName)){
                method= (Method) catchFect.get(catchName);
            }else{
                method = ChildHelperClass.getDeclaredMethod(name, new Class[]{View.class, int.class, boolean.class});
                method.setAccessible(true);
                catchFect.put(catchName,method);
            }
            method.invoke(object, child,index, hidden);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addView(View view, boolean hidden) {
        String name = "addView";
        String catchName=name+"2";
        Method method = null;
        try {
            if(catchFect.containsKey(catchName)){
                method = (Method) catchFect.get(catchName);
            }else{
                method = ChildHelperClass.getDeclaredMethod(name, new Class[]{View.class, boolean.class});
                method.setAccessible(true);
                catchFect.put(catchName,method);
            }
            method.invoke(object, view, hidden);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getChildOffset(int position) {
        String name = "getOffset";
        String catchName=name+"0";
        Method method = null;
        try {
            if(catchFect.containsKey(catchName)){
                method = (Method) catchFect.get(catchName);
            }else{
                method = ChildHelperClass.getDeclaredMethod(name, new Class[]{int.class});
                method.setAccessible(true);
                catchFect.put(catchName,method);
            }
           return(Integer) method.invoke(object,position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
//
    public void removeView(View view) {
        String name = "removeView";
        String catchName=name+"0";
        Method method = null;
        try {
            if(catchFect.containsKey(catchName)){
                method = (Method) catchFect.get(catchName);
            }else{
                method = ChildHelperClass.getDeclaredMethod(name, new Class[]{View.class});
                method.setAccessible(true);
                catchFect.put(catchName,method);
            }
            method.invoke(object, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
