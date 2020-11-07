package xed.storage.util

import java.io.{File, PrintStream}

import com.typesafe.config._
import org.apache.commons.io.FileUtils

import scala.collection.JavaConversions._

/**
  * Created by SangDang on 9/15/16.
  */
object ZConfig {


  private val env = System.getProperty("mode", "local")
  private val config = ConfigFactory.load().withFallback(ConfigFactory.parseFile(new File("conf/" + env + ".conf")))


  private val storagePath = getString("upload_folder")

  val rootFolder: File = new File(storagePath)

  val uploadFolder: File = new File(rootFolder, "upload")
  val serveFolder: File = new File(rootFolder, "serve")

  FileUtils.forceMkdir(rootFolder)
  FileUtils.forceMkdir(uploadFolder)
  FileUtils.forceMkdir(serveFolder)


  val convertVideo: String = getString("converter.video_script")
  var convertAudio: String = getString("converter.audio_script")

  val generateThumbnail: String = getString("converter.generate_img")
  val supportedVideo: String = getString("converter.supported_video")
  val supportedAudio: String = getString("converter.supported_audio")
  val supportedImage: String = getString("converter.supported_img")
  var supportedContent: String = getString("converter.supported_content")

  val listImgSizes: List[String] = getStringList("converter.image_resize", List("800x600"))

  def getNConverterThread() = getInt("converter.nthread", 1)

  def getInt(s: String): Int = config.getInt(s)

  def getInt(s: String, default: Int): Int = if (hasPath(s)) getInt(s) else default

  def getIntList(s: String): Seq[Int] = config.getIntList(s).toIndexedSeq.map(x => x.toInt)

  def getIntList(s: String, default: Seq[Int]): Seq[Int] = if (hasPath(s)) getIntList(s) else default

  def getDouble(s: String): Double = config.getDouble(s)

  def getDouble(s: String, default: Double): Double = if (hasPath(s)) getDouble(s) else default

  def getDoubleList(s: String): List[Double] = config.getDoubleList(s).toList.map(x => x.toDouble)

  def getDoubleList(s: String, default: List[Double]): List[Double] = if (hasPath(s)) getDoubleList(s) else default

  def getLong(s: String): Long = config.getLong(s)

  def getLong(s: String, default: Long): Long = if (hasPath(s)) getLong(s) else default

  def getLongList(s: String): Seq[Long] = config.getLongList(s).toList.map(x => x.toLong)

  def getLongList(s: String, default: List[Long]): Seq[Long] = if (hasPath(s)) getLongList(s) else default

  def getBoolean(s: String): Boolean = config.getBoolean(s)

  def getBoolean(s: String, default: Boolean): Boolean = if (hasPath(s)) getBoolean(s) else default

  def getBooleanList(s: String): List[Boolean] = config.getBooleanList(s).toList.map(x => x.booleanValue())

  def getBooleanList(s: String, default: List[Boolean]): List[Boolean] = if (hasPath(s)) getBooleanList(s) else default

  def getString(s: String): String = config.getString(s)

  def getString(s: String, default: String): String = if (hasPath(s)) getString(s) else default

  def getStringList(s: String): List[String] = config.getStringList(s).toList

  def getStringList(s: String, default: List[String]): List[String] = if (hasPath(s)) getStringList(s) else default

  def getIsNull(s: String): Boolean = config.getIsNull(s)

  def hasPath(s: String): Boolean = config.hasPath(s)

  def print(ps: PrintStream = System.out) = config.entrySet().foreach(x => {
    ps.println(x.getKey + "=" + String.valueOf(x.getValue))
  })


}
