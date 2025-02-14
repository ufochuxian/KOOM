package com.koom.charts

import android.content.Context
import org.json.JSONObject
import java.io.File
import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class HprofChartsMaker {


    // 读取 JSON 数据
    fun loadKoomData(context: Context, fileName: String): JSONObject {
        val file = File(context.filesDir, fileName)
        val jsonString = file.readText()
        return JSONObject(jsonString)
    }

    // 解析泄漏对象
    fun parseLeakObjects(json: JSONObject): List<Pair<String, Int>> {
        val leakObjects = json.getJSONArray("leakObjects")
        val leakList = mutableListOf<Pair<String, Int>>()
        for (i in 0 until leakObjects.length()) {
            val obj = leakObjects.getJSONObject(i)
            val className = obj.getString("className")
            val size = obj.getInt("size")
            leakList.add(Pair(className, size))
        }
        return leakList
    }

    // 解析 GC 引用路径
    fun parseGcPaths(json: JSONObject): List<List<Pair<String, String>>> {
        val gcPaths = json.getJSONArray("gcPaths")
        val pathList = mutableListOf<List<Pair<String, String>>>()
        for (i in 0 until gcPaths.length()) {
            val gc = gcPaths.getJSONObject(i)
            val path = mutableListOf<Pair<String, String>>()
            val refs = gc.getJSONArray("path")
            for (j in 0 until refs.length()) {
                val ref = refs.getJSONObject(j)
                val declaredClass = ref.optString("declaredClass", "Unknown")
                val reference = ref.getString("reference")
                path.add(Pair(declaredClass, reference))
            }
            pathList.add(path)
        }
        return pathList
    }


    fun setupBarChart(barChart: BarChart, leakData: List<Pair<String, Int>>) {
        val entries = leakData.mapIndexed { index, (name, size) ->
            BarEntry(index.toFloat(), size.toFloat())
        }

        val dataSet = BarDataSet(entries, "Memory Leak Size (bytes)").apply {
            color = Color.RED
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(leakData.map { it.first }) // 显示对象名称
            granularity = 1f
        }

        barChart.invalidate() // 刷新图表
    }

    fun setupPieChart(pieChart: PieChart, leakData: List<Pair<String, Int>>) {
        val entries = leakData.map { (name, size) ->
            PieEntry(size.toFloat(), name)
        }

        val dataSet = PieDataSet(entries, "Memory Leak Distribution").apply {
            colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN)
            valueTextColor = Color.BLACK
        }

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.invalidate()
    }

    fun setupGcGraph(graphView: GraphView, gcPaths: List<List<Pair<String, String>>>) {
        val series = LineGraphSeries<DataPoint>()
        var x = 0.0

        for (path in gcPaths) {
            for ((declaredClass, reference) in path) {
                series.appendData(DataPoint(x, x + 1), true, 100)
                x += 1
            }
        }

        graphView.addSeries(series)
    }


}