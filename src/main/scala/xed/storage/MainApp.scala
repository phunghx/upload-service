package xed.storage


import xed.storage.service.VideoConvertService


object MainApp {
  def main(args: Array[String]): Unit = {
    val rServer = new RServer
    if (!rServer.setupAndStart) {
      System.err.println("Could not start rest servers! Exit now.")
      System.exit(1)
    }
    VideoConvertService.start()
  }
}

