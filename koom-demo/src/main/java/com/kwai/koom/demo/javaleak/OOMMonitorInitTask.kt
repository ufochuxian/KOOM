package com.kwai.koom.demo.javaleak

import android.app.Application
import com.kwai.koom.base.InitTask
import com.kwai.koom.base.MonitorLog
import com.kwai.koom.base.MonitorManager
import com.kwai.koom.javaoom.monitor.OOMHprofUploader
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig
import com.kwai.koom.javaoom.monitor.OOMReportUploader
import java.io.File
import java.io.IOException

object OOMMonitorInitTask : InitTask {

  override fun init(application: Application) {
    val appFilesDir = application.filesDir // 获取 app 的 files 目录

    val config = OOMMonitorConfig.Builder()
      .setThreadThreshold(50) //50 only for test! Please use default value!
      .setFdThreshold(300) // 300 only for test! Please use default value!
      .setHeapThreshold(0.9f) // 0.9f for test! Please use default value!
      .setVssSizeThreshold(1_000_000) // 1_000_000 for test! Please use default value!
      .setMaxOverThresholdCount(1) // 1 for test! Please use default value!
      .setAnalysisMaxTimesPerVersion(3) // Consider use default value！
      .setAnalysisPeriodPerVersion(15 * 24 * 60 * 60 * 1000) // Consider use default value！
      .setLoopInterval(5_000) // 5_000 for test! Please use default value!
      .setEnableHprofDumpAnalysis(true)
      // 存储 HPROF 文件
      .setHprofUploader(object : OOMHprofUploader {
        override fun upload(file: File, type: OOMHprofUploader.HprofType) {
          val destFile = File(appFilesDir, file.name) // 复制到 app files 目录
          file.copyTo(destFile, overwrite = true)
          MonitorLog.i("OOMMonitor", "HPROF 文件已保存: ${destFile.absolutePath}")
        }
      })

      // 存储 JSON 报告
      .setReportUploader(object : OOMReportUploader {
        override fun upload(file: File, content: String) {
          val destFile = File(appFilesDir, file.name) // 复制到 app files 目录
          try {
            destFile.writeText(content)
            MonitorLog.i("OOMMonitor", "OOM JSON 报告已保存: ${destFile.absolutePath}")
          } catch (e: IOException) {
            MonitorLog.e("OOMMonitor", "写入 JSON 失败: ${e.message}")
          }
        }
      })
      .build()

    MonitorManager.addMonitorConfig(config)
  }
}