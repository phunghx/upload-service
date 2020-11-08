package tf.storage

import java.io.IOException

import javax.ws.rs.container.{ContainerRequestContext, ContainerResponseContext, ContainerResponseFilter}
import org.eclipse.jetty.servlet.{FilterHolder, ServletContextHandler, ServletHolder}
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.servlet.ServletContainer

class RServer {
  def setupAndStart: Boolean = {
    val server = new JettyServer()
    val resourceConfig = new ResourceConfig
  //  resourceConfig.register(classOf[CORSResponseFilter])
    resourceConfig.register(classOf[MultiPartFeature])
    resourceConfig.packages("xed.storage.controller.http")
    val servletHolder = new ServletHolder(new ServletContainer(resourceConfig))


    val handlers = new ServletContextHandler
    handlers.setContextPath("/")
    handlers.addServlet(servletHolder, "/*")
    setupCORS(handlers)
    server.setup(handlers)
    server.start

  }

  def setupCORS(handler: ServletContextHandler): Unit = {
    import org.eclipse.jetty.servlet.FilterHolder
    import org.eclipse.jetty.servlets.CrossOriginFilter
    import javax.servlet.DispatcherType
    import java.util
    val cors = handler.addFilter(classOf[CrossOriginFilter], "/*", util.EnumSet.of(DispatcherType.REQUEST))
    cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*")
    cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*")
    cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD")
    cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin")
  }
}

class CORSResponseFilter extends ContainerResponseFilter {
  @throws[IOException]
  override def filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext): Unit = {
    val headers = responseContext.getHeaders
    headers.add("Access-Control-Allow-Origin", "*")
    headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
    headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, X-Requested-With, X-Codingpedia")
  }
}