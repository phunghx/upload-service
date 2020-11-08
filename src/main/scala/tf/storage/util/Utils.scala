package tf.storage.util

import java.io.File
import java.util.Calendar
import java.util.regex.Pattern

import org.apache.commons.io.FileUtils

object Utils {

  def getCreatedTime(file: File): String = {
    getCreatedTimeFromFileName(file.getName)
  }

  /** *
    *
    * @param fileName : 1111.mp3 or 1111_400x300.jpg
    * @return 1111
    */
  def getCreatedTimeFromFileName(fileName: String): String = {
    val fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'))
    if (fileNameWithoutExt.contains('_'))
      fileNameWithoutExt.split('_')(0)
    else
      fileNameWithoutExt

  }

  private def ensurePath(path: File): Unit = {
    if (path.exists() == false) {
      FileUtils.forceMkdir(path)
    }
  }


  /**
    *
    * @param createdTime : TimeInMS
    * @return /2018/12/01
    */
  def  getTimePath(createdTime: Long): String = {
    val c = Calendar.getInstance
    c.setTimeInMillis(createdTime.toLong)
    val mYear = c.get(Calendar.YEAR)
    val mMonth = c.get(Calendar.MONTH) + 1
    val mDay = c.get(Calendar.DAY_OF_MONTH)
    return "/" + mYear + "/" + mMonth + "/" + mDay
  }

  def getUploadPath(fileName: String): String = {
    ZConfig.uploadFolder + "/" + fileName
  }

  /**
    *
    * @param fileName 1111.mp4
    * @return serverFolder/video/2018/01/111111.mp4
    *         ./data/serve/video/2018/01/1111.mp4
    */
  def getVideoServePath(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = ZConfig.serveFolder + "/video" + getTimePath(id.toLong)
    ensurePath(new File(path))
    path + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".mp4"
  }

  /**
    *
    * @param fileName 1111.mp4
    * @return serverFolder/video/2018/01/111111.mp4
    *         /video/2018/01/1111.mp4
    */
  def getVideoRelativeUrl(fileName: String): String = {
    val createdTime = getCreatedTimeFromFileName(fileName)
    val path = "/video" + getTimePath(createdTime.toLong)
    path + "/" +  fileName.substring(0, fileName.lastIndexOf(".")) + ".mp4"
  }

  /**
    *
    * @param fileName
    * @return serverFolder/audio/2018/01/111111.mp3
    */
  def getAudioServePath(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = ZConfig.serveFolder + "/audio" + getTimePath(id.toLong)
    ensurePath(new File(path))
    path + "/" + id + ".mp3"
  }

  /**
    *
    * @param fileName 111111.wav
    * @return /audio/2018/01/111111.mp3
    */
  def getAudioRelativeUrl(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = "/audio" + getTimePath(id.toLong)
    path + "/" +  fileName.substring(0, fileName.lastIndexOf(".")) + ".mp3"
  }

  /**
    *
    * @param fileName
    * @return serverFolder/content/2018/01/111111.ext
    */
  def getContentServePath(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path: String = ZConfig.serveFolder + "/content" + getTimePath(id.toLong)
    ensurePath(new File(path))
    path + "/" + id + fileName.substring(fileName.lastIndexOf('.'))
  }

  /**
    *
    * @param fileName 111111.mp3
    * @return /audio/2018/01/111111.mp3
    */
  def getContentRelativeUrl(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = "/content" + getTimePath(id.toLong)
    path + "/" + fileName
  }


  /**
    *
    * @param fileName : 1111_400x300.jpg
    * @return serverFolder/photo/2018/01/11111.jpg
    */
  def getPhotoServePath(fileName: String): String = {
    val createdTime = getCreatedTimeFromFileName(fileName)
    val path: String = ZConfig.serveFolder + "/photo" + getTimePath(createdTime.toLong)
    ensurePath(new File(path))
    path + "/" + fileName
  }

  /**
    *
    * @param fileName 111111.mp3
    * @return /audio/2018/01/111111.mp3
    */
  def getPhotoRelativeUrl(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = "/photo" + getTimePath(id.toLong)
    path + "/" +  fileName.substring(0, fileName.lastIndexOf(".")) + ".jpg"
  }


  /**
    * get serve fileName,
    * if path to fileName not exist, it will make the path
    *
    * @param fileName
    * @return filename.jpg from serve fileName
    */
  def getThumbnailFilePath(fileName: String): String = {
    val id = getCreatedTimeFromFileName(fileName)
    val path = ZConfig.serveFolder + "/video" + getTimePath(id.toLong)
    ensurePath(new File(path))
    return path + "/" + id + ".jpg"
  }

  def getPreServePath(id: Long): String = {
    import java.util.Calendar
    val c = Calendar.getInstance
    c.setTimeInMillis(id)
    val mYear = c.get(Calendar.YEAR) - 1970
    val mMonth = c.get(Calendar.MONTH)
    return ZConfig.serveFolder + "/" + mYear + "/" + mMonth
  }


  /***
    * Return extension from url
    * @param url http://abc.com/apa.jpg?t=1
    * @return .jpg
    */
  def getExtensionFromUrl(url:String): String = {
    val urlWithoutFragment = url.split('#')(0)
    val urlWithoutQuery = urlWithoutFragment.split('?')(0)
    val fileName = urlWithoutQuery.split('/').last
    if(fileName.contains('.')){
      fileName.substring(fileName.lastIndexOf('.'))
    }else{
      null
    }
  }


}
