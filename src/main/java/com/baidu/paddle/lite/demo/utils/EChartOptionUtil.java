package com.baidu.paddle.lite.demo.utils;

import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.code.X;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Line;

public class EChartOptionUtil {
    public static GsonOption getLineChartOptions(Object[] xAxis, Object[] yAxis) {
        GsonOption option = new GsonOption();
        option.title().text("本次训练数据").x(X.center);//标题
//        option.legend("得分曲线");//图例
        option.tooltip().trigger(Trigger.axis);
        ValueAxis valueAxis = new ValueAxis();
        option.yAxis(valueAxis);

        CategoryAxis categorxAxis = new CategoryAxis();
        categorxAxis.axisLine().onZero(true);//不在0刻度上
        categorxAxis.boundaryGap(true);//坐标轴两边留白
        categorxAxis.data(xAxis);//坐标轴数据
        option.xAxis(categorxAxis);//坐标轴

        Line line = new Line();
        line.smooth(true).name("得分曲线").data(yAxis).itemStyle().normal().lineStyle().shadowColor("rgba(0,0,0,0.4)");//

        option.series(line);
        return option;
    }
}
