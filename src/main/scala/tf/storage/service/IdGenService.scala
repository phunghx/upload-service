package tf.storage.service

import java.util.concurrent.atomic.AtomicLong

object IdGenService {


  var latestId: Long = System.currentTimeMillis()

  def genId(): Long = this.synchronized {
    var id = System.currentTimeMillis()
    while (id <= latestId) {
      Thread.sleep(100)
      id = System.currentTimeMillis()
    }
    latestId = id
    id
  }
}
