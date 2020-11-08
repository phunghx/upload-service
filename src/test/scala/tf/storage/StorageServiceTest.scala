package tf.storage

import java.io.{DataInputStream, File, FileInputStream}

import org.scalatest.FlatSpec
import tf.storage.service.StorageService
import tf.storage.util.{Utils, ZConfig}
import tf.storage.util.ZConfig

class StorageServiceTest extends FlatSpec {
  final val storageService = new StorageService()
  "Upload Video Sync" should " convert & generate thumbnail" in {

    val localVideo = "src/test/resources/ex_mov.MOV"
    val is = new DataInputStream(new FileInputStream(new File(localVideo)))
    val relativeUrl = storageService.storeVideoSync(is, "ex_mov.mov")
    assert(relativeUrl != null)

    assert(relativeUrl.startsWith("/video/"))
    assert(relativeUrl.endsWith(".mp4"))

    val convertedVideo = ZConfig.serveFolder + relativeUrl
    val convertedFile = new File(convertedVideo)
    assert(convertedFile.exists())
    assert(convertedFile.length() > 0)


    val generateThumbnail = ZConfig.serveFolder + relativeUrl.split(".mp4")(0) + ".jpg"
    val thumbnail = new File(generateThumbnail)
    assert(thumbnail.exists())
    assert(thumbnail.length() > 0)

    convertedFile.delete()
    thumbnail.delete()
  }

  "Upload Video From URL Sync" should " convert & generate thumbnail" in {
    val urlVideo = "http://media.withamazon.com/static/video/2020/3/23/1584979909897.mp4"
    val fileName = storageService.storeVideoSyncFromUrl(urlVideo, ".mp4")


    assert(fileName != null)

    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val convertedVideo = Utils.getVideoServePath(fileName)
    val convertedFile = new File(convertedVideo)
    assert(convertedFile.exists())
    assert(convertedFile.length() > 0)


    val generateThumbnail = Utils.getThumbnailFilePath(fileName)
    val thumbnail = new File(generateThumbnail)
    assert(thumbnail.exists())
    assert(thumbnail.length() > 0)

    convertedFile.delete()
    thumbnail.delete()
  }

  "Upload Audio Sync " should " convert " in {
    val localAudio = "./video/ex_wav.wav"
    val is = new DataInputStream(new FileInputStream(new File(localAudio)))
    val fileName = storageService.storeAudioSync(is, "ex_wav.wav")
    assert(fileName != null)


    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val convertedAudio = Utils.getAudioServePath(fileName)
    val convertedFile = new File(convertedAudio)
    assert(convertedFile.exists())
    assert(convertedFile.length() > 0)

    convertedFile.delete()

  }
  "Upload Audio From Url Sync " should " convert " in {
    val urlAudio = "http://dev.withamazon.com/video/ex_wav.wav"
    val fileName = storageService.storeAudioSyncFromUrl(urlAudio, ".wav")

    assert(fileName != null)

    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val convertedAudio = Utils.getAudioServePath(fileName)
    val convertedFile = new File(convertedAudio)
    assert(convertedFile.exists())
    assert(convertedFile.length() > 0)

    convertedFile.delete()
  }

  "Upload Content Sync" should " store content in serve folder" in {
    val localImage = "./video/ex.jpg"
    val is = new DataInputStream(new FileInputStream(new File(localImage)))
    val fileName = storageService.storeContent(is, "ex.jpg")
    assert(fileName != null)
    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val contentServePath = Utils.getContentServePath(fileName)
    val contentServeFile = new File(contentServePath)
    assert(contentServeFile.exists())
    assert(contentServeFile.length() > 0)
    contentServeFile.delete()
  }


  "Upload Photo Sync" should " resize then store photo in serve folder " in {
    val localImage = "./resources/ex.jpg"
    val is = new DataInputStream(new FileInputStream(new File(localImage)))
    val fileName = storageService.storeImage(is, "ex.jpg")
    assert(fileName != null)
    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val photoServePath = Utils.getPhotoServePath(fileName)
    val photoServeFile = new File(photoServePath)
    assert(photoServeFile.exists())
    assert(photoServeFile.length() > 0)

  }
  "Upload Photo From Url Sync " should " convert " in {
    val urlAudio = "http://media.withamazon.com/static/photo/2020/3/23/1584979627313_800x600.jpg"
    val fileName = storageService.storeAudioSyncFromUrl(urlAudio, ".wav")

    assert(fileName != null)

    val fileNameAndExt = fileName.split('.')
    assert(fileNameAndExt.length == 2)

    val convertedAudio = Utils.getAudioServePath(fileName)
    val convertedFile = new File(convertedAudio)
    assert(convertedFile.exists())
    assert(convertedFile.length() > 0)

    convertedFile.delete()
  }



}
