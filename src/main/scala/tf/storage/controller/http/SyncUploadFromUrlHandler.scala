package tf.storage.controller.http

import java.io.InputStream

import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Context
import javax.ws.rs.{GET, POST, Path, Produces}
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.glassfish.jersey.media.multipart.{FormDataContentDisposition, FormDataParam}
import tf.storage.service.StorageService
import tf.storage.util.{Utils, ZConfig}
import tf.storage.util.ZConfig

@Path("/upload_from_url")
@Produces(Array("text/plain"))
class SyncUploadFromUrlHandler {
  final val logger = Logger.getLogger("FileUploadHandler")
  val storageService: StorageService = new StorageService()
  @Context
  var _request: HttpServletRequest = null

  @GET
  def getUpload: String = {
    val is = _request.getInputStream
    "get: " + is.available()
  }

  @POST
  def upload(@FormDataParam("file") _url: String,
             @FormDataParam("token") token: String): String = {
    val startTime = System.currentTimeMillis()
    try {

      val url = if (_url.startsWith("http")) _url else "http://" + _url

      val fileExtension = Utils.getExtensionFromUrl(url)
      if (fileExtension == null) {
        throw new Exception(url + " invalid")
      }
      val isVideo = ZConfig.supportedVideo.contains(fileExtension)
      val isAudio = ZConfig.supportedAudio.contains(fileExtension)
      val isContent = ZConfig.supportedContent.contains(fileExtension)

      val servePath = if (isVideo) {
        storageService.storeVideoSyncFromUrl(url, fileExtension)
      } else if (isAudio) {
        storageService.storeAudioSyncFromUrl(url, fileExtension)
      } else if(isContent){
        storageService.storeContentSyncFromUrl(url, fileExtension)
      } else {
        throw new Exception(s"Unsupported exception: $url with ext: '$fileExtension''")
      }

      Stats.addUploadFromUrl(System.currentTimeMillis() - startTime, result = true)
      buildResponseSuccess(servePath)
    } catch {
      case ex: Throwable => {
        logger.error("FileUploadHandler::upload_from_url" + ex.getMessage)
        Stats.addUploadFromUrl(System.currentTimeMillis() - startTime, result = false)
        buildResponseFail(ex)
      }
    }

  }


  protected def buildResponseFail(ex: Throwable): String = {
    val err = new JsonObject
    err.addProperty("reason", String.valueOf(ex.getMessage))
    err.addProperty("message", String.valueOf(ex.getCause))

    val obj = new JsonObject
    obj.addProperty("success", false)
    obj.add("data", err)
    obj.toString
  }

  protected def buildResponseSuccess(id: String): String = {
    val obj = new JsonObject
    obj.addProperty("success", true)
    obj.addProperty("data", id)
    obj.toString
  }
}
