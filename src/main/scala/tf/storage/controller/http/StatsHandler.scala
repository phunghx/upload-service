package tf.storage.controller.http

import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

import javax.servlet.http
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import Stats.statsUploadImg

case class ActionStats(name: String) {
  val nTotal: AtomicLong = new AtomicLong()
  val nTimeInMS: AtomicLong = new AtomicLong()
  val nSuccess: AtomicLong = new AtomicLong()

  def add(processTime: Long, result: Boolean) = {
    nTotal.incrementAndGet()
    if (result == true) {
      nSuccess.incrementAndGet()
    }
    nTimeInMS.addAndGet(processTime);
  }

  def toHTML(): String = {
    s"""
       |<p> $name
       |<table>
       |  <tr>
       |    <td> Total </td>
       |    <td> TimeInMS </td>
       |    <td> Num Success </td>
       | </tr>
       |  <tr>
       |    <td> $nTotal</td>
       |    <td> $nTimeInMS </td>
       |    <td> $nSuccess </td>
       | </tr>
       |</table>

    """.stripMargin
  }
}

object Stats {
  private val statsUpload = ActionStats("upload_general")
  private val statsUploadFromUrl = ActionStats("upload_general")
  private val statsUploadContent = ActionStats("upload_general_content")
  private val statsUploadImg = ActionStats("upload_img_content")
  private val statsConvertVideo = ActionStats("convert_video")
  private val statsConvertAudio = ActionStats("convert_audio")
  private val statsGenerateThumbnail = ActionStats("generate_thumbnail")


  def addUpload(timeInMS: Long, result: Boolean): Long = {
    statsUpload.add(timeInMS, result)
  }

  def addUploadFromUrl(timeInMS: Long, result: Boolean): Long = {
    statsUploadFromUrl.add(timeInMS, result)
  }

  def addConvertVideo(timeInMS: Long, result: Boolean): Long = {
    statsConvertVideo.add(timeInMS, result)
  }

  def addConvertAudio(timeInMS: Long, result: Boolean): Long = {
    statsConvertAudio.add(timeInMS, result)
  }

  def addGenerateThumb(timeInMS: Long, result: Boolean): Long = {
    statsGenerateThumbnail.add(timeInMS, result)
  }

  def addUploadContent(timeInMS: Long, result: Boolean): Long = {
    statsUploadContent.add(timeInMS, result)
  }

  def addUploadImage(timeInMS: Long, result: Boolean): Long = {
    statsUploadImg.add(timeInMS, result)
  }

  def toHtml: String = {
    s"""
       |${statsUpload.toHTML()}
       |<br>
       |${statsUploadFromUrl.toHTML()}
       |<br>
       |${statsConvertVideo.toHTML()}
       |<br>
       |${statsGenerateThumbnail.toHTML()}
       |<br>
       |${statsConvertAudio.toHTML()}
       |<br>
       |${statsUploadContent.toHTML()}
       |
       |
      """.stripMargin

  }


}

@Path("/stats")
@Produces(Array("text/html"))
class StatsHandler {


  @Context
  var _request: HttpServletRequest = null


  @GET def stats(): String = {
    Stats.toHtml
  }


}