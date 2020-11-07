package xed.storage.controller.http

import java.io.InputStream

import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Context
import javax.ws.rs.{GET, POST, Path, Produces}
import org.apache.log4j.Logger
import org.glassfish.jersey.media.multipart.{FormDataContentDisposition, FormDataParam}
import xed.storage.service.StorageService
import xed.storage.util.{Utils, ZConfig}

@Path("/upload")
@Produces(Array("text/plain"))
class SyncFileUploadHandler {
  final val logger = Logger.getLogger("FileUploadHandler")
  val storageService: StorageService = new StorageService()
  @Context
  var _request: HttpServletRequest = null

  @GET
  def getUpload(): String = {
    var is = _request.getInputStream
    "get: " + is.available()
  }

  @POST
  def upload(@FormDataParam("file") is: InputStream,
             @FormDataParam("file") data: FormDataContentDisposition,
             @FormDataParam("token") token: String): String = {
    val startTime = System.currentTimeMillis()
    try {
      val fileName = data.getFileName.trim.toLowerCase
      val fileExtension = fileName.substring(fileName.lastIndexOf('.'))
      val isVideo = ZConfig.supportedVideo.contains(fileExtension)
      val isAudio = ZConfig.supportedAudio.contains(fileExtension)
      val isPhoto = ZConfig.supportedImage.contains(fileExtension)
      val isContent = ZConfig.supportedContent.contains(fileExtension)
      val servePath = if (isVideo) {
        storageService.storeVideoSync(is, fileName)
      }
      else if (isAudio) {
        storageService.storeAudioSync(is, fileName)

      }
      else if (isPhoto) {
        storageService.storeImage(is, fileName)
      }
      else if (isContent) {
        storageService.storeContent(is, fileName)
      } else {
        throw new Exception("Unsupported Exception ")
      }


      Stats.addUpload(System.currentTimeMillis() - startTime, true)
      buildResponseSuccess(servePath)
    } catch {
      case ex: Throwable => {
        logger.error("FileUploadHandler::upload" + ex.getMessage)
        Stats.addUpload(System.currentTimeMillis() - startTime, false)
        return buildResponseFail(ex)
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
