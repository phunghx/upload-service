package tf.storage.service

import java.io._
import java.net.URL

import com.sun.corba.se.impl.naming.cosnaming.NamingUtils
import net.coobird.thumbnailator.Thumbnails
import org.apache.commons.io.{FileUtils, IOUtils}
import tf.storage.controller.http.Stats
import tf.storage.util.{Utils, ZConfig}
import tf.storage.util.ZConfig

class StorageService {


  /** *
    *
    * @param extension .jpg or jpg is also ok
    * @return File(./data/upload-folder/1111111.jpg)
    */
  def getUploadFile(extension: String): File = {
    val fileId = IdGenService.genId()
    val newFileName = if (extension.startsWith(".")) fileId + extension else fileId + "." + extension
    new File(ZConfig.uploadFolder, newFileName)

  }


  def storeAudioSync(is: InputStream, fileName: String): String = {
    val file = getUploadFile(fileName.substring(fileName.indexOf('.')))
    FileUtils.copyInputStreamToFile(is, file)
    VideoConvertService.processAudio(file.getName)
    Utils.getAudioRelativeUrl(file.getName)
  }

  def storeAudioSyncFromUrl(url: String, extension: String): String = {
    val file = getUploadFile(extension)
    FileUtils.copyURLToFile(new URL(url), file)
    VideoConvertService.processAudio(file.getName)
    Utils.getAudioRelativeUrl(file.getName)
  }

  /**
    *
    * @param url       http://a.com/b.jpg?query=1
    * @param extension file Extension: .jpg or jpg
    * @return Relative URl /video/2020/03/12/11111.jpg
    */
  def storeVideoSyncFromUrl(url: String, extension: String): String = {
    val file = getUploadFile(extension)
    FileUtils.copyURLToFile(new URL(url), file)
    VideoConvertService.processVideo(file.getName)
    Utils.getVideoRelativeUrl(file.getName)
  }

  def storeVideoSync(is: InputStream, fileName: String): String = {
    val file = getUploadFile(fileName.substring(fileName.indexOf('.')))
    FileUtils.copyInputStreamToFile(is, file)
    VideoConvertService.processVideo(file.getName)
    Utils.getVideoRelativeUrl(file.getName)
  }

  def storeVideo(is: InputStream, fileName: String): String = {
    val file = getUploadFile(fileName.substring(fileName.indexOf('.')))
    FileUtils.copyInputStreamToFile(is, file)

    VideoConvertService.addToConvertList(file.getName)
    Utils.getVideoRelativeUrl(file.getName)
  }

  /** *
    * Resize image and store to serve folder
    *
    * @param is       : Input stream contain img bytes data
    * @param fileName : image fileName
    * @return: largest image file in serve folder
    */

  def storeImage(is: InputStream, fileName: String): String = {
    val startTime = System.currentTimeMillis()
    try {
      //store medium file
      //      val newFileName = IdGenService.genId() + fileName.substring(fileName.lastIndexOf('.'))
      //      val file = new File(Utils.getContentServePath(newFileName))
      //      FileUtils.copyInputStreamToFile(is, file)
      val baos = new ByteArrayOutputStream()
      IOUtils.copy(is, baos)
      val bais = new ByteArrayInputStream(baos.toByteArray)
      //resizeThenWrite file
      var lastSuccessFileName = ""
      val photoId = IdGenService.genId()
      for (size <- ZConfig.listImgSizes) {
        bais.reset()
        val intSize = size.split('x').map(_.toInt)

        if (intSize.length == 2) {
          //photo file name: 1111_400x300.jpg
          val newFileName = s"${photoId}_$size.jpg"
          val file = new File(Utils.getPhotoServePath(newFileName))
          resizeThenWrite(bais, file, intSize(0), intSize(1), true)
          lastSuccessFileName = file.getName
        }
      }
      Stats.addUploadImage(System.currentTimeMillis() - startTime, true)
      Utils.getPhotoRelativeUrl(lastSuccessFileName)
    } catch {
      case ex: Throwable => {
        Stats.addUploadContent(System.currentTimeMillis() - startTime, false)
        throw ex
      }
    }
  }

  /** *
    * Directly store to serve folder
    *
    * @param is
    * @param fileName
    * @return
    */

  def storeContent(is: InputStream, fileName: String): String = {
    val startTime = System.currentTimeMillis()
    try {
      val newFileName = IdGenService.genId() + fileName.substring(fileName.lastIndexOf('.'))
      val file = new File(Utils.getContentServePath(newFileName))
      FileUtils.copyInputStreamToFile(is, file)
      Stats.addUploadContent(System.currentTimeMillis() - startTime, true)
      Utils.getContentRelativeUrl(file.getName)
    } catch {
      case ex: Throwable => {
        Stats.addUploadContent(System.currentTimeMillis() - startTime, false)
        throw ex
      }
    }
  }

  def storeContentSyncFromUrl(url: String, extension: String): String = {
    val startTime = System.currentTimeMillis()
    try {
      val newFileName = IdGenService.genId() + extension
      val file = new File(Utils.getContentServePath(newFileName))
      FileUtils.copyURLToFile(new URL(url), file)
      Stats.addUploadContent(System.currentTimeMillis() - startTime, true)
      Utils.getContentRelativeUrl(file.getName)
    } catch {
      case ex: Throwable => {
        Stats.addUploadContent(System.currentTimeMillis() - startTime, false)
        throw ex
      }
    }
  }


  def resizeImg(is: InputStream, os: OutputStream, w: Int, h: Int, keepAspectRatio: Boolean): Unit = {
    Thumbnails.of(is).height(h).keepAspectRatio(keepAspectRatio).outputFormat("jpg").toOutputStream(os)
  }

  def resizeThenWrite(is: InputStream, toFile: File, w: Int, h: Int, keepAspectRatio: Boolean): Unit = {
    Thumbnails.of(is).height(h).keepAspectRatio(keepAspectRatio).outputFormat("jpg").toFile(toFile)
  }


}
