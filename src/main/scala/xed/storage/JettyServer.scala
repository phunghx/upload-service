package xed.storage

import java.net.BindException

import org.apache.log4j.Logger
import org.eclipse.jetty.server
import org.eclipse.jetty.server.{Connector, Server}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.thread.QueuedThreadPool
import xed.storage.util.ZConfig

class JettyServer {

  protected var _host = "0.0.0.0"
  protected var _port = ZConfig.getInt("server.port")
  protected var _nconnectors = ZConfig.getInt("nconnector", 1)
  protected var _nacceptors = ZConfig.getInt("nacceptor", 4)
  protected var _acceptQueueSize = ZConfig.getInt("queue_size", 1000)
  protected var _nminThreads = ZConfig.getInt("min_thread", 10)
  protected var _nmaxThreads: Int = this._nminThreads * 2
  protected var _maxIdleTime = ZConfig.getInt("max_idle_time", 5000)
  protected var _connMaxIdleTime: Int = this._maxIdleTime
  protected var _threadMaxIdleTime: Int = this._maxIdleTime
  protected var _server: server.Server = null
  protected var _thread: Thread = null
  protected var _logger: Logger = Logger.getLogger("JettyServer")

  init()
  final protected def init(): Unit = {
    this._server = new org.eclipse.jetty.server.Server()
    val _threadPool = new QueuedThreadPool
    _threadPool.setName("JettyThreadPoolServer")
    _threadPool.setMinThreads(this._nminThreads)
    _threadPool.setMaxThreads(this._nmaxThreads)
    _threadPool.setMaxIdleTimeMs(this._maxIdleTime)
    this._server.setThreadPool(_threadPool)
    val connectors = new Array[Connector](this._nconnectors)
    var i = 0
    while ( {
      i < this._nconnectors
    }) {
      val connector1 = new SelectChannelConnector
      connector1.setHost(this._host)
      connector1.setPort(this._port + i)
      connector1.setMaxIdleTime(this._connMaxIdleTime)
      connector1.setAcceptQueueSize(this._acceptQueueSize)
      connector1.setThreadPool(_threadPool)
      connector1.setAcceptors(this._nacceptors)
      connectors(i) = connector1
      i = i + 1
    }
    this._server.setConnectors(connectors)
  }

  def start: Boolean = {
    if (this._server == null) return false
    var result = false
    try {
      this._server.start()
      this._thread = new Thread(new ServerRunner(this._server), "JettyWebServerRunner")
      this._thread.start()
      result = true
    } catch {
      case ex: BindException =>
        this._logger.error(null, ex)
        stop
      case ex: Exception =>
        this._logger.error(null, ex)
        stop
    }
    result
  }

  def stop: Boolean = {
    try {
      this._server.stop()
      this._thread.join()
      this._thread = null
      return true
    } catch {
      case ex: Exception =>
        this._logger.error(null, ex)
    }
    false
  }

  def setup(handler: ServletContextHandler): Unit = {
    this._server.setHandler(handler)
  }

  protected class ServerRunner(val _server: Server) extends Runnable {
    override def run(): Unit = {
      _logger.info("Web server is going to serve")
      try
        _server.join()
      catch {
        case ex: Exception =>
          _logger.error(null, ex)
      }
      _logger.info("Web Server is going to stopped")
    }
  }

}