package com.koom.charts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.jjoe64.graphview.GraphView

class LeakAnalysisActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LeakAnalysisActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leak_analysis)

        val hprofChartsMaker = HprofChartsMaker()

        // **尝试加载 JSON 数据**
        val jsonFileName = "1.0.0_2025-02-13_17-23-05_216.json"
        val json = hprofChartsMaker.loadKoomData(this, jsonFileName)

        if (json == null) {
            Toast.makeText(this, "数据加载失败: $jsonFileName", Toast.LENGTH_LONG).show()
            return
        }

        val leakData = hprofChartsMaker.parseLeakObjects(json)
        val gcPaths = hprofChartsMaker.parseGcPaths(json)

        // **设置柱状图**
        val barChart = findViewById<BarChart>(R.id.barChart)
        hprofChartsMaker.setupBarChart(barChart, leakData)

        // **设置饼图**
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        hprofChartsMaker.setupPieChart(pieChart, leakData)

        // **设置 GC 引用路径网络图**
        val graphView = findViewById<GraphView>(R.id.graphView)
        hprofChartsMaker.setupGcGraph(graphView, gcPaths)

        // **动态调整 GraphView 高度**
        adjustGraphViewHeight(graphView, gcPaths.size)
    }

    // **根据 GC 数据量动态调整 GraphView 高度**
    private fun adjustGraphViewHeight(graphView: GraphView, dataSize: Int) {
        val newHeight = (dataSize * 100).coerceAtMost(1200) // 限制最大高度 1200dp
        val params = graphView.layoutParams as LinearLayout.LayoutParams
        params.height = newHeight
        graphView.layoutParams = params
    }
}
