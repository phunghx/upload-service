package tf.storage.service

import java.io.{File, FilenameFilter}
import java.util
import java.util.concurrent
import java.util.concurrent._
import java.util.function.Consumer

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, IOFileFilter, TrueFileFilter}
import org.apache.log4j.Logger
import tf.storage.controller.http.Stats
import VideoConvertService.{_convertAudio, deleteUploadFile}
import tf.storage.util.{Utils, ZConfig}
import tf.storage.util.ZConfig

import sys.process._

/**
  *
  * Convert video to new format
  */

object VideoConvertService {

  val nConvertThread = ZConfig.getNConverterThread()

  val ignoreLists = new ConcurrentSkipListSet[String]()
  val waitingLists = new LinkedBlockingDeque[String]()

  val logger = Logger.getLogger("VideoConvertService")

  val mediaFileFilter = new IOFileFilter {
    override def accept(file: File): Boolean = {
      try {
        if (file.isFile) {

          val name = file.getName
          val names = name.split('.')
          if (names.length != 2) {
            false
          } else {
            val id = names(0)
            val format = names(1)
            return id.toLong > 0 && (ZConfig.supportedVideo.contains(format) || ZConfig.supportedAudio.contains(format))
          }
        }
        false

      } catch {
        case _: Throwable => false
      }
    }

    override def accept(file: File, name: String): Boolean = {
      try {
        if (file.isFile) {
          val id = name.substring(0, name.lastIndexOf('.'))
          return id.toLong > 0
        }
        false
      } catch {
        case _ => false

      }
    }
  }

  def start(): Unit = {
    logger.info(s"VideoConvertService::start() Start Video Convert Worker: n:$nConvertThread")
    for (i <- 0 to nConvertThread) {
      val worker = new Thread(converter)
      worker.setName("video_converter_" + i)
      worker.start()
    }
    startMonitor()
  }


  private def startMonitor(): Unit = {
    val monitorThread = new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val files: util.Collection[File] = FileUtils.listFilesAndDirs(ZConfig.uploadFolder, mediaFileFilter, DirectoryFileFilter.INSTANCE)

          if (files.size() > 0) {
            files.forEach(new Consumer[File] {
              override def accept(file: File): Unit = {
                if (file.isFile && !waitingLists.contains(file.getName) && !ignoreLists.contains(file.getName)) {
                  logger.info(s"Add $file to waitingLists" + waitingLists.size())
                  waitingLists.add(file.getName)
                }
              }
            })
          } else {
            ignoreLists.clear()
          }
          Thread.sleep(86400000L)
        }
      }
    })
    monitorThread.start()
  }

  def addToConvertList(fileName: String): Unit =
    waitingLists.add(fileName)

  var normalLines = 0
  var errorLines = 0
  val countLogger = ProcessLogger(line => normalLines += 1,
    line => errorLines += 1)

  /** *
    * Lookup fileName in folder
    *
    * @return
    */
  def converter: Runnable = new Runnable {

    override def run(): Unit = {
      while (true) {
        try {
          val fileName = waitingLists.take()
          if (fileName == null) {
            Thread.sleep(1000)
          } else {
            logger.info(Thread.currentThread().getName + " process: " + fileName)
            val fileExtension = fileName.split('.')(1)
            if (ZConfig.supportedVideo.contains(fileExtension)) {
              if (_convertVideo(fileName)) {
                _convertThumbnail(fileName)
                deleteUploadFile(fileName)
              }
            }
            else if (ZConfig.supportedAudio.contains(fileExtension)) {
              if (_convertAudio(fileName)) {
                deleteUploadFile(fileName)
              }
            } else {
              throw new UnsupportedOperationException("Unsupported extension: " + fileExtension)
            }

          }
        }
        catch {
          case ex: Throwable => logger.error(ex)
        }
      }
    }


  }

  /**
    * Convert Video to mp4 standard
    * Generate Thumbnail
    * Delete original file if successful
    *
    * @param fileName
    * @return
    */
  def processVideo(fileName: String): Boolean = {
    try {
      if (_convertVideo(fileName)) {
        _convertThumbnail(fileName)
        deleteUploadFile(fileName)
        true
      }
      false
    } catch {
      case throwable: Throwable => {
        logger.error(throwable)
        false
      }
    }
  }

  def processAudio(fileName: String): Boolean = {
    try {

      if (_convertAudio(fileName)) {
        deleteUploadFile(fileName)
        return true
      }
      false
    } catch {
      case throwable: Throwable => {
        logger.error(throwable)
        false
      }
    }
  }


  private def deleteUploadFile(fileName: String): Unit = {
    try {
      ignoreLists.add(fileName)
      val file = new File(Utils.getUploadPath(fileName))
      logger.info("delete " + file.getAbsolutePath)
      if (file != null && file.exists() && file.isFile) {
        file.delete()
      }
    } catch {
      case _ => logger.error("error when delete fileName " + fileName)
    }
  }

  /**
    * Convert fileName from upload folder to serve folder
    *
    * @param fileName
    * @return
    */
  private def _convertVideo(fileName: String): Boolean = {

    val startTime = System.currentTimeMillis()

    val uploadPath = Utils.getUploadPath(fileName)
    val servePath = Utils.getVideoServePath(fileName)

    val convertScript = ZConfig.convertVideo.format(uploadPath, servePath)

    val convertResult = convertScript !! (countLogger);
    if (convertResult != null && convertResult.length > 0)
      logger.info(convertResult)

    if (convertResult != null && convertResult.length > 0)
      logger.info(convertResult)

    val convertedFile = new File(servePath)
    val result = convertedFile.exists() && convertedFile.length() > 0
    Stats.addConvertVideo(System.currentTimeMillis() - startTime, result)
    return result
  }


  private def _convertThumbnail(fileName: String): Boolean = {
    val startTime = System.currentTimeMillis()

    val servePath = Utils.getVideoServePath(fileName)
    val thumbnailPath = Utils.getThumbnailFilePath(fileName)
    val generateThumbnailScript = ZConfig.generateThumbnail.format(servePath, thumbnailPath)
    val generateThumbnailResult = generateThumbnailScript !!;
    if (generateThumbnailResult != null && generateThumbnailResult.length > 0)
      logger.info(generateThumbnailResult)

    val thumbnailFile = new File(thumbnailPath)
    val result = thumbnailFile.exists() && thumbnailFile.length() > 0
    Stats.addGenerateThumb(System.currentTimeMillis() - startTime, result)
    result
  }

  private def _convertAudio(fileName: String): Boolean = {
    val startTime = System.currentTimeMillis()
    val uploadPath = Utils.getUploadPath(fileName)
    val servePath = Utils.getAudioServePath(fileName)
    val convertScript = ZConfig.convertAudio.format(uploadPath, servePath)
    val convertResult = convertScript !!;
    if (convertResult != null && convertResult.length > 0)
      logger.info(convertResult)

    val convertedFile = new File(servePath)
    val result = convertedFile.exists() && convertedFile.length() > 0

    Stats.addConvertAudio(System.currentTimeMillis() - startTime, result)

    return result
  }

}