package tf.storage

import org.scalatest.FlatSpec
import tf.storage.util.Utils

class UtilsTest extends FlatSpec {

  "Valid URL " should "return extension" in {
    val ext1 = Utils.getExtensionFromUrl("http://x.education/abc.jpg?tere#1")
    assert(ext1.equals(".jpg"))

    val ext2 = Utils.getExtensionFromUrl("http://x.education/abc.mp3?query=1&query2=xyz")
    assert(ext2.equals(".mp3"))

    val ext3 = Utils.getExtensionFromUrl("x.education/n/x/abc.mp4?query=1&query2=xyz#1")
    assert(ext3.equals(".mp4"))
  }

  "Invalid URL " should "return null" in {
    val ext1 = Utils.getExtensionFromUrl("http://x.education/abc?tere#1")
    assert(ext1 == null)
  }

}
